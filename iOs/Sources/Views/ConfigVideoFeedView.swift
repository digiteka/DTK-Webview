import SwiftUI

struct ConfigVideoFeedView: View {
    static let defaultConsentString = "CQlo8kAQlo8kAAHABBENCiFsAP_gAEPgAAAALAEB7C_cRSFicSZn4LsgSQxewUhCoMAhBAIIACwBiAIAJJwG1mECIAjAgCAKABIAICRAAQAlCADABAAAAIABITCEIEAQARAAIqBAAAARQgIACAhAGQAAGAAQgMJUAgEAkAMECBqoQFhAAQAgigAQIAAlAICFAAAAAAAgQAAAIAAAAmwQEgAAcAIAEAAAAFEMAAAAoAECAAAAEAAQAAAAQBAAAAAAAAAgAQAIgAQAAAAABBYAgPYX7iKQsTiQI_BdkASGL2CkAVBgEIIBBAASAMQBABJGAWswgRAEQAAQBAAIABASIAAAEIAAIAIAAABAAJCIQgAgCACIAABQIAAACKEAAAAEIASAAAwACEBhKgAAgEAAggANUCAsAACAEEUAABAAAoBAQgAAAAAAECAAABAAAAEyAAkAADgBAAgAAAAIhgAAAFAAAQAAAAgACAAAACAAAAAAAAAAEAAABEACAAAAAAIAwSADAAEGSB0AGAAIMkEIAMAAQZIJQAYAAgyQUgAwABBkgtABgACDJAAA.ILAEB7C_cRSFicSZn4LsgSQxewUhCoMAhBAIIACwBiAIAJJwG1mECIAjAgCAKABIAICRAAQAlCADABAAAAIABITCEIEAQARAAIqBAAAARQgIACAhAGQAAGAAQgMJUAgEAkAMECBqoQFhAAQAgigAQIAAlAICFAAAAAAAgQAAAIAAAAmwQEgAAcAIAEAAAAFEMAAAAoAECAAAAEAAQAAAAQBAAAAAAAAAgAQAIgAQAAAAABAA.f_wAH_wAAAAA"

    @AppStorage("videoFeedMdtk") private var mdtk = "01573101"
    @AppStorage("videoFeedZoneId") private var zoneId = 0
    @AppStorage("videoFeedAdunitPath") private var adunitPath = ""
    @AppStorage("videoFeedVideoId") private var videoId = ""
    @AppStorage("videoFeedCarrouselHeight") private var carrouselHeight = 280
    @AppStorage("videoFeedVfBranch") private var vfBranch = ""
    @AppStorage("videoFeedCarrBranch") private var carrBranch = ""
    @AppStorage("videoFeedConsentStringEnabled") private var consentStringEnabled = true

    private var consentString: String? {
        consentStringEnabled ? Self.defaultConsentString : nil
    }

    private var zoneIdText: Binding<String> {
        Binding(
            get: { zoneId == 0 ? "" : String(zoneId) },
            set: { zoneId = Int($0) ?? 0 }
        )
    }

    private var carrouselHeightText: Binding<String> {
        Binding(
            get: { String(carrouselHeight) },
            set: { carrouselHeight = Int($0) ?? 0 }
        )
    }

    private enum VFBranchMode: String, CaseIterable, Identifiable {
        case local = "Local"
        case recette = "Recette"
        case production = "Production"
        var id: String { rawValue }
    }

    private var vfBranchMode: VFBranchMode {
        if vfBranch.hasPrefix(HTMLGenerator.localBranchPrefix) { return .local }
        if vfBranch.hasPrefix(HTMLGenerator.recetteBranchPrefix) { return .recette }
        return vfBranch.isEmpty ? .production : .recette
    }

    private var vfBranchModeBinding: Binding<VFBranchMode> {
        Binding(
            get: { vfBranchMode },
            set: { newMode in
                switch newMode {
                case .local: vfBranch = HTMLGenerator.localBranchPrefix
                case .recette: vfBranch = HTMLGenerator.recetteBranchPrefix
                case .production: vfBranch = ""
                }
            }
        )
    }

    private var localHostText: Binding<String> {
        Binding(
            get: { String(vfBranch.dropFirst(HTMLGenerator.localBranchPrefix.count)) },
            set: { vfBranch = HTMLGenerator.localBranchPrefix + $0 }
        )
    }

    private var recetteBranchText: Binding<String> {
        Binding(
            get: {
                vfBranch.hasPrefix(HTMLGenerator.recetteBranchPrefix)
                    ? String(vfBranch.dropFirst(HTMLGenerator.recetteBranchPrefix.count))
                    : vfBranch
            },
            set: { vfBranch = HTMLGenerator.recetteBranchPrefix + $0 }
        )
    }

    private enum CarrBranchMode: String, CaseIterable, Identifiable {
        case local = "Local"
        case recette = "Recette"
        case production = "Production"
        var id: String { rawValue }
    }

    private var carrBranchMode: CarrBranchMode {
        if carrBranch.hasPrefix(HTMLGenerator.localBranchPrefix) { return .local }
        if carrBranch.hasPrefix(HTMLGenerator.recetteBranchPrefix) { return .recette }
        return .production
    }

    private var carrBranchModeBinding: Binding<CarrBranchMode> {
        Binding(
            get: { carrBranchMode },
            set: { newMode in
                switch newMode {
                case .local: carrBranch = HTMLGenerator.localBranchPrefix
                case .recette: carrBranch = HTMLGenerator.recetteBranchPrefix
                case .production: carrBranch = ""
                }
            }
        )
    }

    private var carrLocalHostText: Binding<String> {
        Binding(
            get: { String(carrBranch.dropFirst(HTMLGenerator.localBranchPrefix.count)) },
            set: { carrBranch = HTMLGenerator.localBranchPrefix + $0 }
        )
    }

    private var carrRecetteBranchText: Binding<String> {
        Binding(
            get: {
                carrBranch.hasPrefix(HTMLGenerator.recetteBranchPrefix)
                    ? String(carrBranch.dropFirst(HTMLGenerator.recetteBranchPrefix.count))
                    : carrBranch
            },
            set: { carrBranch = HTMLGenerator.recetteBranchPrefix + $0 }
        )
    }

    var body: some View {
        Form {
            Section {
                LabeledRow("MDTK") {
                    TextField("ex: 01xxxxxx", text: $mdtk)
                        .multilineTextAlignment(.trailing)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                }

                LabeledRow("Zone ID") {
                    TextField("ex: 3", text: zoneIdText)
                        .multilineTextAlignment(.trailing)
                        .keyboardType(.numberPad)
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text("Adunit Path")
                        .font(.subheadline)
                    TextField("ex: /1234/adunit/path", text: $adunitPath)
                        .font(.footnote.monospaced())
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                        .foregroundStyle(adunitPath.isEmpty ? .secondary : .primary)
                }

                LabeledRow("Video ID") {
                    TextField("ex: 3v83mr3", text: $videoId)
                        .multilineTextAlignment(.trailing)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                }

                LabeledRow("Hauteur du carrousel (px)") {
                    TextField("", text: carrouselHeightText)
                        .multilineTextAlignment(.trailing)
                        .keyboardType(.numberPad)
                }

                VStack(alignment: .leading, spacing: 4) {
                    Picker("Consent String", selection: $consentStringEnabled) {
                        Text("Oui").tag(true)
                        Text("Non").tag(false)
                    }
                    .pickerStyle(.menu)
                }
            } header: {
                Text("Configuration")
            }

            Section {
                VStack(alignment: .leading, spacing: 4) {
                    Text("VF_BRANCH")
                        .font(.subheadline)
                    Picker("VF_BRANCH", selection: vfBranchModeBinding) {
                        ForEach(VFBranchMode.allCases) { mode in
                            Text(mode.rawValue).tag(mode)
                        }
                    }
                    .pickerStyle(.segmented)

                    switch vfBranchMode {
                    case .local:
                        TextField("ex: 192.168.1.136:5173", text: localHostText)
                            .font(.footnote.monospaced())
                            .autocorrectionDisabled()
                            .textInputAutocapitalization(.never)
                            .keyboardType(.numbersAndPunctuation)
                    case .recette:
                        TextField("Branche Amplify, ex: SUP-123", text: recetteBranchText)
                            .font(.footnote.monospaced())
                            .autocorrectionDisabled()
                            .textInputAutocapitalization(.never)
                    case .production:
                        EmptyView()
                    }
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text("CARR_BRANCH")
                        .font(.subheadline)
                    Picker("CARR_BRANCH", selection: carrBranchModeBinding) {
                        ForEach(CarrBranchMode.allCases) { mode in
                            Text(mode.rawValue).tag(mode)
                        }
                    }
                    .pickerStyle(.segmented)

                    switch carrBranchMode {
                    case .local:
                        TextField("ex: 192.168.1.136:5174", text: carrLocalHostText)
                            .font(.footnote.monospaced())
                            .autocorrectionDisabled()
                            .textInputAutocapitalization(.never)
                            .keyboardType(.numbersAndPunctuation)
                    case .recette:
                        TextField("Branche Amplify, ex: SUP-123", text: carrRecetteBranchText)
                            .font(.footnote.monospaced())
                            .autocorrectionDisabled()
                            .textInputAutocapitalization(.never)
                    case .production:
                        EmptyView()
                    }
                }
            } header: {
                Text("Tests")
            }
        }
        .navigationTitle("Configuration VideoFeed")
        .navigationBarTitleDisplayMode(.inline)
    }
}
