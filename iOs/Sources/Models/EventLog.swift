import Foundation

struct EventLog: Identifiable {
    let id = UUID()
    let timestamp: Date
    let rawEvent: String
    let category: Category

    enum Category {
        case video, ad, player, console, unknown

        var emoji: String {
            switch self {
            case .video:   return "🎬"
            case .ad:      return "📢"
            case .player:  return "⚙️"
            case .console: return "🖥️"
            case .unknown: return "💬"
            }
        }
    }

    var formattedTime: String {
        let f = DateFormatter()
        f.dateFormat = "HH:mm:ss.SSS"
        return f.string(from: timestamp)
    }

    static func categorize(_ event: String) -> Category {
        let lower = event.lowercased()
        if lower.hasPrefix("[console.") { return .console }
        if lower.contains("video")      { return .video }
        if lower.contains("ad")         { return .ad }
        if lower.contains("player")     { return .player }
        return .unknown
    }
}
