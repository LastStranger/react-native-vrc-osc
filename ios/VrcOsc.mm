#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(VrcOsc, RCTEventEmitter)

RCT_EXTERN_METHOD(createClient:(NSString *)address
                  port:(NSNumber *)port)

RCT_EXTERN_METHOD(sendMessage:(NSString *)address
                  data:(NSArray *)data)

RCT_EXTERN_METHOD(createServer:(NSString *)address
                  port:(NSNumber *)port)

@end
