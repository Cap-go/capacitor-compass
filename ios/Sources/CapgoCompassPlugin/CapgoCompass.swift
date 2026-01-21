import Foundation
import CoreLocation
import os.log

@objc public class CapgoCompass: NSObject, CLLocationManagerDelegate {
    private let locationManager = CLLocationManager()
    private let log = OSLog(subsystem: "app.capgo.capacitor.compass", category: "CapgoCompass")

    private var lastTrueHeading: Double = -1.0
    private var headingCallback: ((Double) -> Void)?
    private var permissionCallback: (() -> Void)?

    // Default throttling constants
    private static let defaultMinIntervalSeconds: TimeInterval = 0.1  // Max 10 events/sec
    private static let defaultMinHeadingChange: Double = 2.0  // Minimum 2° change

    // Configurable throttling values
    private var minIntervalSeconds: TimeInterval = defaultMinIntervalSeconds
    private var minHeadingChange: Double = defaultMinHeadingChange

    // Throttling state
    private var lastNotifyTime: TimeInterval = 0
    private var lastNotifiedHeading: Double = -1.0

    @objc override public init() {
        super.init()
        locationManager.delegate = self
        // Set heading filter to reduce event frequency
        locationManager.headingFilter = 1.0  // Only notify when heading changes by at least 1°
    }

    @objc public func requestPermission(completion: @escaping () -> Void) {
        permissionCallback = completion
        locationManager.requestWhenInUseAuthorization()
    }

    public func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        // Only call the callback if permission was just requested
        if let callback = permissionCallback {
            callback()
            permissionCallback = nil
        }
    }

    /// Configure throttling parameters.
    /// - Parameters:
    ///   - minIntervalMs: Minimum interval between events in milliseconds (default: 100)
    ///   - minHeadingChange: Minimum heading change in degrees to trigger an event (default: 2.0)
    @objc public func setThrottling(minIntervalMs: Int, minHeadingChange: Double) {
        self.minIntervalSeconds = minIntervalMs > 0 ? Double(minIntervalMs) / 1000.0 : CapgoCompass.defaultMinIntervalSeconds
        self.minHeadingChange = minHeadingChange > 0 ? minHeadingChange : CapgoCompass.defaultMinHeadingChange
    }

    @objc public func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
        let heading = newHeading.trueHeading
        if heading < 0 || newHeading.headingAccuracy < 0 {
            return
        }
        self.lastTrueHeading = heading

        guard let callback = headingCallback else { return }
        let now = Date().timeIntervalSince1970

        // Time-based throttle
        if now - lastNotifyTime < minIntervalSeconds {
            return  // Skip - too soon
        }

        // Heading change threshold (with wraparound handling)
        if lastNotifiedHeading >= 0 {
            var diff = abs(heading - lastNotifiedHeading)
            if diff > 180 { diff = 360 - diff }
            if diff < minHeadingChange {
                return  // Skip - heading hasn't changed enough
            }
        }

        lastNotifyTime = now
        lastNotifiedHeading = heading
        callback(heading)
    }

    @objc public func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        os_log("Location manager failed with error: %{public}@", log: log, type: .error, error.localizedDescription)
    }

    @objc public func startListeners() {
        // Reset throttling state
        lastNotifyTime = 0
        lastNotifiedHeading = -1.0

        if CLLocationManager.headingAvailable() {
            locationManager.startUpdatingLocation()
            locationManager.startUpdatingHeading()
        } else {
            os_log("CLLocationManager heading not available", log: log, type: .error)
        }
    }

    @objc public func stopListeners() {
        locationManager.stopUpdatingLocation()
        locationManager.stopUpdatingHeading()
    }

    @objc public func getCurrentHeading() -> Double {
        return self.lastTrueHeading
    }

    @objc public func setHeadingCallback(_ callback: ((Double) -> Void)?) {
        self.headingCallback = callback
    }

    public func getAuthorizationStatus() -> CLAuthorizationStatus {
        if #available(iOS 14.0, *) {
            return locationManager.authorizationStatus
        } else {
            return CLLocationManager.authorizationStatus()
        }
    }
}
