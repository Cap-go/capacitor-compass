import Foundation
import CoreLocation
import os.log

@objc public class CapgoCompass: NSObject, CLLocationManagerDelegate {
    private let locationManager = CLLocationManager()
    private let log = OSLog(subsystem: "app.capgo.capacitor.compass", category: "CapgoCompass")

    private var lastTrueHeading: Double = -1.0
    private var headingCallback: ((Double) -> Void)?
    private var permissionCallback: (() -> Void)?
    
    // Throttling state
    private var lastReportedHeading: Double = -1.0
    private var lastReportedTime: TimeInterval = 0
    private var minHeadingChange: Double = 2.0
    private var minInterval: TimeInterval = 0.1 // 100ms in seconds

    @objc override public init() {
        super.init()
        locationManager.delegate = self
        // Set built-in heading filter to reduce native event frequency
        locationManager.headingFilter = 1.0
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

    @objc public func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
        self.lastTrueHeading = newHeading.trueHeading
        
        if shouldReportHeading(newHeading.trueHeading) {
            lastReportedHeading = newHeading.trueHeading
            lastReportedTime = Date().timeIntervalSince1970
            headingCallback?(newHeading.trueHeading)
        }
    }

    @objc public func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        os_log("Location manager failed with error: %{public}@", log: log, type: .error, error.localizedDescription)
    }

    @objc public func startListeners() {
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
    
    @objc public func setThrottlingOptions(minHeadingChange: Double, minInterval: Double) {
        self.minHeadingChange = minHeadingChange
        self.minInterval = minInterval / 1000.0 // Convert ms to seconds
    }
    
    private func shouldReportHeading(_ heading: Double) -> Bool {
        let currentTime = Date().timeIntervalSince1970
        
        // Time-based throttling
        if currentTime - lastReportedTime < minInterval {
            return false
        }
        
        // Change-based throttling
        if lastReportedHeading >= 0 {
            var headingDelta = abs(heading - lastReportedHeading)
            // Handle wraparound (e.g., 359° -> 1° should be 2° difference, not 358°)
            if headingDelta > 180 {
                headingDelta = 360 - headingDelta
            }
            if headingDelta < minHeadingChange {
                return false
            }
        }
        
        return true
    }

    public func getAuthorizationStatus() -> CLAuthorizationStatus {
        return locationManager.authorizationStatus
    }
}
