import Foundation

enum TriggerMode: String, CaseIterable, Identifiable {
    case none
    case onClick
    case scrollToPlay
    case autoplay

    var id: String { rawValue }

    var label: String {
        switch self {
        case .none: return "Par défaut"
        case .onClick: return "Click To Play"
        case .scrollToPlay: return "Scroll To Play"
        case .autoplay: return "Autoplay"
        }
    }

    /// Valeur du paramètre `/autoplay/` de l'iframe ultimedia — nil si aucun mode sélectionné (paramètre omis).
    var autoplayValue: Int? {
        switch self {
        case .none: return nil
        case .onClick: return 0
        case .scrollToPlay: return 2
        case .autoplay: return 1
        }
    }
}
