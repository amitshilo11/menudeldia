import ComposeApp

/// Implements the KMP IosAuthBridge protocol, combining Apple and Google sign-in.
/// Pass an instance of this class to MainViewControllerKt.MainViewController(bridge:).
class CompositeAuthBridge: IosAuthBridge {

    private let appleBridge = AppleSignInBridge()
    private let googleBridge = GoogleSignInBridge()

    func signInWithApple(rawNonce: String, onResult: @escaping (String?, String?) -> Void) {
        appleBridge.signIn(rawNonce: rawNonce, onResult: onResult)
    }

    func signInWithGoogle(onResult: @escaping (String?, String?) -> Void) {
        googleBridge.signIn(onResult: onResult)
    }

    func signOut() {
        googleBridge.signOut()
    }
}
