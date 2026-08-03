import Foundation
import Combine

class PlayerViewModel: ObservableObject {
    @Published var logs: [EventLog] = []

    func addEvent(_ event: String) {
        let log = EventLog(
            timestamp: Date(),
            rawEvent: event,
            category: EventLog.categorize(event)
        )
        DispatchQueue.main.async {
            self.logs.insert(log, at: 0)
            if self.logs.count > 300 {
                self.logs = Array(self.logs.prefix(300))
            }
        }
    }

    /// Spécialisé pour les logs console JS — catégorisé automatiquement comme `.console`
    func addConsoleLog(_ message: String) {
        let log = EventLog(
            timestamp: Date(),
            rawEvent: message,
            category: .console
        )
        DispatchQueue.main.async {
            self.logs.insert(log, at: 0)
            if self.logs.count > 300 {
                self.logs = Array(self.logs.prefix(300))
            }
        }
    }

    func clear() {
        DispatchQueue.main.async {
            self.logs = []
        }
    }
}
