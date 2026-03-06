import SwiftUI
import BizTrackerShared

struct ContentView: View {
    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                Image(systemName: "chart.line.uptrend.xyaxis.circle.fill")
                    .font(.system(size: 48))
                    .foregroundStyle(.teal)
                Text("BizTracker iOS")
                    .font(.title2.weight(.semibold))
                Text("KMP shared framework is connected.")
                    .font(.body)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
            }
            .padding(24)
            .navigationTitle("Dashboard")
        }
    }
}

#Preview {
    ContentView()
}
