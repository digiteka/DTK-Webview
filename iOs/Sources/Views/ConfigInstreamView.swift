import SwiftUI

struct ConfigInstreamView: View {
    @AppStorage("mdtk") private var mdtk = "01211820"
    @AppStorage("src") private var src = "3v83mr3"
    @AppStorage("zone") private var zone = "3"
    @AppStorage("refererURL") private var refererURL = "https://www.digiteka.com"
    @AppStorage("consentString") private var consentString = ""
    @AppStorage("newplayerDomain") private var newplayer = ""

    private var iframeURLPreview: String {
        HTMLGenerator.iframeURL(
            mdtk: mdtk.isEmpty ? "[MDTK]" : mdtk,
            zone: zone.isEmpty ? "[Zone]" : zone,
            src: src.isEmpty ? "[SRC]" : src,
            autoplay: 1,
            sound: 1,
            ad: 1,
            refererURL: refererURL,
            consentString: consentString,
            newplayer: newplayer.isEmpty ? nil : newplayer
        )
    }

    var body: some View {
        Form {
            // ── Champs de configuration ──────────────────────────────────────
            Section {
                LabeledRow("MDTK") {
                    TextField("ex: 01xxxxxx", text: $mdtk)
                        .multilineTextAlignment(.trailing)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                }
                LabeledRow("Zone") {
                    TextField("ex: 3", text: $zone)
                        .multilineTextAlignment(.trailing)
                        .keyboardType(.numberPad)
                }
                LabeledRow("SRC") {
                    TextField("ex: 3v83mr3", text: $src)
                        .multilineTextAlignment(.trailing)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                }
                VStack(alignment: .leading, spacing: 4) {
                    Text("URL référent")
                        .font(.subheadline)
                    TextField("facultatif — ex: https://monsite.com", text: $refererURL)
                        .font(.footnote.monospaced())
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                        .keyboardType(.URL)
                        .foregroundStyle(refererURL.isEmpty ? .secondary : .primary)
                }
                VStack(alignment: .leading, spacing: 4) {
                    Text("Newplayer")
                        .font(.subheadline)
                    TextField("facultatif — ex: prod", text: $newplayer)
                        .font(.footnote.monospaced())
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                        .keyboardType(.URL)
                        .foregroundStyle(newplayer.isEmpty ? .secondary : .primary)
                    Text("prod ou https://192.168.XXX.XXX:YYY/dist ou https://XXX.d2sdl16pluelsx.amplifyapp.com")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            } header: {
                Text("Configuration")
            }

            // ── Résumé de la configuration active ───────────────────────────
            Section {
                paramRow(label: "MDTK", value: mdtk, placeholder: "Non renseigné")
                paramRow(label: "Zone", value: zone, placeholder: "Non renseignée")
                paramRow(label: "SRC", value: src, placeholder: "Non renseigné")
                paramRow(label: "Referer", value: refererURL, placeholder: "Aucun")
                LabeledRow("Newplayer") {
                    Text(newplayer.isEmpty ? "désactivé" : newplayer)
                        .foregroundStyle(newplayer.isEmpty ? .tertiary : .primary)
                        .font(.footnote.monospaced())
                        .lineLimit(1)
                        .textSelection(.enabled)
                }

                VStack(alignment: .leading, spacing: 6) {
                    Text("URL iframe")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    Text(iframeURLPreview)
                        .font(.caption2.monospaced())
                        .foregroundStyle(.secondary)
                        .lineLimit(6)
                        .textSelection(.enabled)
                }
                .padding(.vertical, 2)
            } header: {
                Text("Résumé de la configuration active")
            } footer: {
                Text("L'URL ci-dessus est générée en temps réel à partir des paramètres saisis.")
            }
        }
        .navigationTitle("Config Instream")
        .navigationBarTitleDisplayMode(.inline)
    }

    @ViewBuilder
    private func paramRow(label: String, value: String, placeholder: String) -> some View {
        LabeledRow(label) {
            Text(value.isEmpty ? placeholder : value)
                .foregroundStyle(value.isEmpty ? .tertiary : .primary)
                .font(.footnote.monospaced())
                .lineLimit(1)
                .textSelection(.enabled)
        }
    }
}
