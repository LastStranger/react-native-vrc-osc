import Foundation
import SwiftOSC
import React


@objc(VrcOsc)
class VrcOsc: RCTEventEmitter, OSCServerDelegate {

    var client: OSCClient!
    var server: OSCServer!

    @objc(createClient:port:)
    func createClient(address: String, port: NSNumber) -> Void {
        client = OSCClient(address: address, port: port.intValue)
    }

    @objc(sendMessage:data:)
    func sendMessage(address: String, data: NSArray) -> Void {
        let message = OSCMessage(OSCAddressPattern(address))

        for value in data {
            switch value {
            case let someInt as Int:
                message.add(someInt)
            case let someDouble as Double where someDouble > 0:
                message.add(someDouble)
            case let someBool as Bool:
                message.add(someBool)
            case let someString as String:
                message.add(someString)
            default:
                print("不支持的数据类型")
            }
        }

        client.send(message)
    }


    var hasListeners = false

    override func startObserving() {
        hasListeners = true
    }

    override func stopObserving() {
        hasListeners = false
    }

    @objc override func invalidate() {
        print("[Swift VrcOsc] Invalidate called, releasing server")
        if server != nil {
            server = nil
        }
        super.invalidate()
    }

    @objc(createServer:port:)
    func createServer(address: String, port: NSNumber) -> Void {
        print("[Swift VrcOsc] createServer called with address: '\(address)', port: \(port.intValue)")
        if server != nil {
            print("[Swift VrcOsc] Stopping existing OSC server")
            server = nil
        }
        if port.intValue > 0 {
            print("[Swift VrcOsc] Starting OSC server on port: \(port.intValue)")
            server = OSCServer(address: address, port: port.intValue)
            server.delegate = self
            server.start()
        }
    }

    func didReceive(_ message: OSCMessage) {
        print("[Swift VrcOsc] Received OSC message: \(message.address.string) -> \(message.arguments)")
        if hasListeners {
            let response: NSMutableDictionary = [:]
            response["address"] = message.address.string
            response["data"] = message.arguments
            sendEvent(withName: "GotMessage", body: response)
        } else {
            print("[Swift VrcOsc] Ignored message, no JS listeners registered yet")
        }
    }

    override func supportedEvents() -> [String]! {
        return ["GotMessage"]
    }

    override static func requiresMainQueueSetup() -> Bool {
        return false
    }
}
