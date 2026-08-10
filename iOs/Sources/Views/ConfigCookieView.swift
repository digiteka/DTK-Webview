import SwiftUI
import WebKit

private let cookieDomain = "www.ultimedia.com"
private let cookieDomainMatch = "ultimedia.com"

struct ConfigCookieView: View {
    // Mêmes clés @AppStorage que ConfigInstreamView — l'URL d'injection réutilise exactement le
    // même endpoint iframe déjà prouvé joignable, plutôt que la racine du domaine (jamais testée).
    @AppStorage("mdtk") private var mdtk = "01211820"
    @AppStorage("src") private var src = "3v83mr3"
    @AppStorage("zone") private var zone = "3"

    @State private var cookies: [HTTPCookie] = []
    @State private var name = ""
    @State private var value = ""
    @State private var editingCookieName: String?
    @State private var toast: String?

    private var cookieStore: WKHTTPCookieStore {
        WKWebsiteDataStore.default().httpCookieStore
    }

    var body: some View {
        Form {
            Section {
                LabeledRow("Nom") {
                    TextField("debug", text: $name)
                        .multilineTextAlignment(.trailing)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                        .disabled(editingCookieName != nil)
                }
                LabeledRow("Valeur") {
                    TextField("true", text: $value)
                        .multilineTextAlignment(.trailing)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                }

                Button(editingCookieName == nil ? "Ajouter" : "Mettre à jour") {
                    addOrUpdateCookie()
                }
                .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)

                if editingCookieName != nil {
                    Button("Annuler la modification", role: .cancel) {
                        cancelEditing()
                    }
                }
            } header: {
                Text("Cookies — \(cookieDomain)")
            }

            Section {
                if cookies.isEmpty {
                    Text("Aucun cookie")
                        .foregroundStyle(.secondary)
                } else {
                    ForEach(cookies, id: \.name) { cookie in
                        HStack {
                            Button {
                                startEditing(cookie)
                            } label: {
                                HStack {
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(cookie.name)
                                            .foregroundStyle(.primary)
                                        Text(cookie.value)
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                            .lineLimit(1)
                                    }
                                    Spacer()
                                    Image(systemName: "pencil")
                                        .foregroundStyle(.secondary)
                                }
                            }
                            .buttonStyle(.plain)

                            Button {
                                deleteCookie(cookie)
                            } label: {
                                Image(systemName: "trash")
                                    .foregroundStyle(.red)
                            }
                            .buttonStyle(.plain)
                        }
                        .swipeActions {
                            Button(role: .destructive) {
                                deleteCookie(cookie)
                            } label: {
                                Label("Supprimer", systemImage: "trash")
                            }
                        }
                    }
                }
            } header: {
                Text("Cookies actifs (\(cookies.count))")
            }

            Section {
                Button(role: .destructive) {
                    deleteAllCookies()
                } label: {
                    Text("Tout supprimer")
                        .frame(maxWidth: .infinity)
                        .multilineTextAlignment(.center)
                }
                .disabled(cookies.isEmpty)
            }
        }
        .navigationTitle("Configuration Cookie")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear(perform: refresh)
        .alert(
            toast ?? "",
            isPresented: Binding(get: { toast != nil }, set: { if !$0 { toast = nil } })
        ) {
            Button("OK", role: .cancel) { toast = nil }
        }
    }

    private func refresh() {
        cookieStore.getAllCookies { allCookies in
            let filtered = allCookies
                .filter { $0.domain.hasSuffix(cookieDomainMatch) }
                .sorted { $0.name < $1.name }
            DispatchQueue.main.async {
                cookies = filtered
            }
        }
    }

    private func startEditing(_ cookie: HTTPCookie) {
        editingCookieName = cookie.name
        name = cookie.name
        value = cookie.value
    }

    private func cancelEditing() {
        editingCookieName = nil
        name = ""
        value = ""
    }

    private func addOrUpdateCookie() {
        let trimmedName = name.trimmingCharacters(in: .whitespaces)
        guard !trimmedName.isEmpty else { return }
        let trimmedValue = value.trimmingCharacters(in: .whitespaces)

        // 11 mois, converti en Max-Age (secondes) — Expires requis pour que le cookie survive à
        // la navigation, sinon WebKit le traite en session-only et le perd au retour sur l'écran.
        let expiresDate = Calendar.current.date(byAdding: .month, value: 11, to: Date()) ?? Date()
        let maxAge = Int(expiresDate.timeIntervalSinceNow)

        let setCookieString = "\(trimmedName)=\(trimmedValue); Domain=\(cookieDomain); Path=/; Max-Age=\(maxAge); Secure; SameSite=None"

        let pageURL = HTMLGenerator.iframeURL(
            mdtk: mdtk.isEmpty ? "01211820" : mdtk,
            zone: zone.isEmpty ? "3" : zone,
            src: src.isEmpty ? "3v83mr3" : src,
            autoplay: 0,
            sound: 0,
            ad: 0,
            refererURL: "https://www.ultimedia.com",
            consentString: ""
        )

        CookieInjector.setCookie(setCookieString, onPage: pageURL) { result in
            DispatchQueue.main.async {
                switch result {
                case .success:
                    cancelEditing()
                    refresh()
                case .failure(let error):
                    toast = "Échec création cookie : \(error.localizedDescription)"
                }
            }
        }
    }

    private func deleteCookie(_ cookie: HTTPCookie) {
        cookieStore.delete(cookie) {
            DispatchQueue.main.async {
                if editingCookieName == cookie.name { cancelEditing() }
                refresh()
            }
        }
    }

    private func deleteAllCookies() {
        let group = DispatchGroup()
        cookies.forEach { cookie in
            group.enter()
            cookieStore.delete(cookie) { group.leave() }
        }
        group.notify(queue: .main) {
            toast = "Tous les cookies supprimés"
            cancelEditing()
            refresh()
        }
    }
}
