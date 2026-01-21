import CoreLocation
import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(CapgoCompassPlugin)
public class CapgoCompassPlugin: CAPPlugin, CAPBridgedPlugin {
    private let pluginVersion: String = "8.1.3"
    public let identifier = "CapgoCompassPlugin"
    public let jsName = "CapgoCompass"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "getCurrentHeading", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getPluginVersion", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "startListening", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "stopListening", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "checkPermissions", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "requestPermissions", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = CapgoCompass()
    private var isListening = false
    private var permissionCallId: String?

    override public func load() {
        implementation.startListeners()

        NotificationCenter.default.addObserver(
            forName: UIApplication.didBecomeActiveNotification,
            object: nil,
            queue: OperationQueue.main
        ) { [weak self] _ in
            self?.implementation.startListeners()
        }

        NotificationCenter.default.addObserver(
            forName: UIApplication.willResignActiveNotification,
            object: nil,
            queue: OperationQueue.main
        ) { [weak self] _ in
            self?.implementation.stopListeners()
        }
    }

    @objc func getCurrentHeading(_ call: CAPPluginCall) {
        let heading = implementation.getCurrentHeading()
        if heading < 0 {
            call.reject("Failed to get heading. Did the user permit location access?")
            return
        }
        call.resolve([
            "value": heading
        ])
    }

    @objc func getPluginVersion(_ call: CAPPluginCall) {
        call.resolve(["version": self.pluginVersion])
    }

    @objc func startListening(_ call: CAPPluginCall) {
        if isListening {
            call.resolve()
            return
        }

        // Parse optional throttling configuration
        let minInterval = call.getInt("minInterval")
        let minHeadingChange = call.getDouble("minHeadingChange")

        let intervalMs = minInterval ?? 100
        let headingChange = minHeadingChange ?? 2.0
        implementation.setThrottling(minIntervalMs: intervalMs, minHeadingChange: headingChange)

        isListening = true
        implementation.setHeadingCallback { [weak self] heading in
            guard let self = self else { return }
            if heading >= 0 {
                self.notifyListeners("headingChange", data: [
                    "value": heading
                ])
            }
        }

        call.resolve()
    }

    @objc func stopListening(_ call: CAPPluginCall) {
        if !isListening {
            call.resolve()
            return
        }

        isListening = false
        implementation.setHeadingCallback(nil)

        call.resolve()
    }

    @objc override public func checkPermissions(_ call: CAPPluginCall) {
        call.resolve(["compass": currentPermissionState()])
    }

    @objc override public func requestPermissions(_ call: CAPPluginCall) {
        let status = implementation.getAuthorizationStatus()

        if status == .notDetermined {
            // Store call ID and request permission
            permissionCallId = call.callbackId
            implementation.requestPermission { [weak self] in
                guard let self = self else { return }
                if let callId = self.permissionCallId,
                   let savedCall = self.bridge?.savedCall(withID: callId) {
                    savedCall.resolve(["compass": self.currentPermissionState()])
                    self.bridge?.releaseCall(savedCall)
                    self.permissionCallId = nil
                }
            }
            bridge?.saveCall(call)
        } else {
            call.resolve(["compass": currentPermissionState()])
        }
    }

    private func currentPermissionState() -> String {
        let status = implementation.getAuthorizationStatus()
        switch status {
        case .authorizedAlways, .authorizedWhenInUse:
            return "granted"
        case .denied, .restricted:
            return "denied"
        case .notDetermined:
            return "prompt"
        @unknown default:
            return "prompt"
        }
    }
}
