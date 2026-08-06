import Foundation

enum NewplayerMode: String, CaseIterable, Identifiable {
    case legacy
    case prod
    case recette
    case local

    var id: String { rawValue }

    var label: String {
        switch self {
        case .legacy: return "Player Legacy"
        case .prod: return "Player 2026"
        case .recette: return "Branche"
        case .local: return "Local"
        }
    }

    /// Valeur du paramètre `newplayer` à concaténer à l'URL ultimedia, selon le mode sélectionné.
    func resolvedValue(branchName: String, localIP: String) -> String? {
        switch self {
        case .legacy:
            return nil
        case .prod:
            return "prod"
        case .recette:
            return branchName.isEmpty ? nil : "https://\(branchName).d2sdl16pluelsx.amplifyapp.com"
        case .local:
            return localIP.isEmpty ? nil : "https://\(localIP)/dist"
        }
    }
}
