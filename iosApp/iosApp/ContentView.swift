import UIKit
import SwiftUI
import MenudizApp

struct ComposeView: UIViewControllerRepresentable {

    private let bridge = CompositeAuthBridge()

    func makeUIViewController(context: Context) -> UIViewController {
        let controller = MainViewControllerKt.MainViewController(bridge: bridge)
        // Avoids a black flash between the launch screen and Compose's first frame.
        controller.view.backgroundColor = MenuTheme_iosKt.primaryUIColor()
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}



