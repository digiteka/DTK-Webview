import SwiftUI

@main
struct DTKTesterApp: App {

    init() {
        // Initialise les valeurs par défaut si elles sont absentes ou vides.
        // register(defaults:) est insuffisant ici car une chaîne vide "" stockée
        // en UserDefaults prend toujours la priorité sur les défauts Swift.
        let defaults = UserDefaults.standard
        if defaults.string(forKey: "mdtk")?.isEmpty != false {
            defaults.set("01211820", forKey: "mdtk")
        }
        if defaults.string(forKey: "zone")?.isEmpty != false {
            defaults.set("3", forKey: "zone")
        }
        if defaults.string(forKey: "src")?.isEmpty != false {
            defaults.set("3v83mr3", forKey: "src")
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
