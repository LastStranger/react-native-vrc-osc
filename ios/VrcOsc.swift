import Foundation
import SwiftOSC
import React


@objc(VrcOsc)
class VrcOsc: RCTEventEmitter, OSCServerDelegate {

    var client: OSCClient!
    var server: OSCServer!

    @objc(createClient:port:)
    func createClient(address: String, port: NSNumber) -> Void {
        if client == nil || client.address != address || client.port != port.intValue {
            client = OSCClient(address: address, port: port.intValue)
        } else {
            print("Client with the same address and port already exists.")
        }
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


    @objc(createServer:port:)
    func createServer(address: String, port: NSNumber) -> Void {
        server = OSCServer(address: address, port: port.intValue)
        server.delegate = self
        server.start()
    }

    func didReceive(_ message: OSCMessage) {
        let response: NSMutableDictionary = [:]
        response["address"] = message.address.string
        response["data"] = message.arguments
        sendEvent(withName: "GotMessage", body: response)
    }

    override func supportedEvents() -> [String]! {
        return ["GotMessage"]
    }

    override static func requiresMainQueueSetup() -> Bool {
        return false
    }
}
