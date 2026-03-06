# BizTracker iOS App

This folder contains an Xcode app target (`BizTracker`) already wired for Kotlin Multiplatform direct integration.

## Open in Xcode

Open:

- `BizTracker/BizTracker.xcodeproj`

## What is already configured

- SwiftUI app target and entry files:
  - `BizTracker/BizTracker/BizTrackerApp.swift`
  - `BizTracker/BizTracker/ContentView.swift`
- Build phase script to compile/embed shared KMP framework:
  - `./gradlew :shared:embedAndSignAppleFrameworkForXcode`
- Framework search path for generated KMP output:
  - `$(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`
- Linker flag for `BizTrackerShared` framework.

## One-time local setup (macOS)

Install full Xcode (not only Command Line Tools), then run:

```bash
sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer
sudo xcodebuild -license
xcodebuild -version
```

## Build notes

- `embedAndSignAppleFrameworkForXcode` must be run by Xcode build (it needs Xcode environment variables).
- In Xcode 15+, keep `Enable User Script Sandboxing = No` for this target (already set in project file).
- Set your Apple Team in Signing settings before running on a device.
