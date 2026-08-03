import SwiftUI

/// Équivalent iOS 15 de `LabeledContent` (disponible seulement à partir d'iOS 16).
/// Libellé aligné à gauche, contenu poussé à droite — même rendu visuel dans un `Form`.
struct LabeledRow<Content: View>: View {
    private let label: String
    private let content: Content

    init(_ label: String, @ViewBuilder content: () -> Content) {
        self.label = label
        self.content = content()
    }

    var body: some View {
        HStack(spacing: 12) {
            Text(label)
            Spacer(minLength: 12)
            content
        }
    }
}
