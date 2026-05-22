import AuthenticationServices
import CryptoKit
import Foundation

/// Handles Sign in with Apple.
/// Receives a plain rawNonce from KMP, SHA-256 hashes it, sets it on the
/// Apple request, then returns the identity token string via the callback.
class AppleSignInBridge: NSObject {

    private var onResult: ((String?, String?) -> Void)?

    func signIn(rawNonce: String, onResult: @escaping (String?, String?) -> Void) {
        self.onResult = onResult

        let provider = ASAuthorizationAppleIDProvider()
        let request = provider.createRequest()
        request.requestedScopes = [.fullName, .email]
        request.nonce = sha256(rawNonce)

        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.presentationContextProvider = self
        controller.performRequests()
    }

    private func sha256(_ input: String) -> String {
        let data = Data(input.utf8)
        let digest = SHA256.hash(data: data)
        return digest.compactMap {
            String(format: "%02x", $0)
        }
        .joined()
    }
}

extension AppleSignInBridge: ASAuthorizationControllerDelegate {

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization authorization: ASAuthorization
    ) {
        guard
            let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
            let tokenData = credential.identityToken,
            let token = String(data: tokenData, encoding: .utf8)
        else {
            onResult?(nil, "Apple Sign-In: missing identity token")
            onResult = nil
            return
        }
        onResult?(token, nil)
        onResult = nil
    }

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError error: Error
    ) {
        onResult?(nil, error.localizedDescription)
        onResult = nil
    }
}

extension AppleSignInBridge: ASAuthorizationControllerPresentationContextProviding {
    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        guard
            let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
            let window = scene.windows.first(where: { $0.isKeyWindow }) ?? scene.windows.first
        else {
            return UIWindow()
        }
        return window
    }
}
