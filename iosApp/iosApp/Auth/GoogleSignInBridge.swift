import GoogleSignIn
import UIKit

/// Handles Sign in with Google via GIDSignIn.
/// Add the GoogleSignIn Swift Package to the Xcode project:
///   File → Add Package Dependencies → https://github.com/google/GoogleSignIn-iOS
class GoogleSignInBridge {

    func signIn(onResult: @escaping (String?, String?) -> Void) {
        guard let rootVC = rootViewController() else {
            onResult(nil, "Google Sign-In: no root view controller")
            return
        }
        GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { result, error in
            if let error = error {
                onResult(nil, error.localizedDescription)
                return
            }
            guard let idToken = result?.user.idToken?.tokenString else {
                onResult(nil, "Google Sign-In: missing ID token")
                return
            }
            onResult(idToken, nil)
        }
    }

    func signOut() {
        GIDSignIn.sharedInstance.signOut()
    }

    private func rootViewController() -> UIViewController? {
        guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene else {
            return nil
        }
        return (scene.windows.first(where: { $0.isKeyWindow }) ?? scene.windows.first)?
            .rootViewController
    }
}
