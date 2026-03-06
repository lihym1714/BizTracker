#import <Foundation/NSArray.h>
#import <Foundation/NSDictionary.h>
#import <Foundation/NSError.h>
#import <Foundation/NSObject.h>
#import <Foundation/NSSet.h>
#import <Foundation/NSString.h>
#import <Foundation/NSValue.h>

@class BTSCategorySum, BTSConstants, BTSDailySum, BTSDateUtils, BTSEntryNoteCodec, BTSKotlinPair<__covariant A, __covariant B>, BTSSummary;

NS_ASSUME_NONNULL_BEGIN
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunknown-warning-option"
#pragma clang diagnostic ignored "-Wincompatible-property-type"
#pragma clang diagnostic ignored "-Wnullability"

#pragma push_macro("_Nullable_result")
#if !__has_feature(nullability_nullable_result)
#undef _Nullable_result
#define _Nullable_result _Nullable
#endif

__attribute__((swift_name("KotlinBase")))
@interface BTSBase : NSObject
- (instancetype)init __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
+ (void)initialize __attribute__((objc_requires_super));
@end

@interface BTSBase (BTSBaseCopying) <NSCopying>
@end

__attribute__((swift_name("KotlinMutableSet")))
@interface BTSMutableSet<ObjectType> : NSMutableSet<ObjectType>
@end

__attribute__((swift_name("KotlinMutableDictionary")))
@interface BTSMutableDictionary<KeyType, ObjectType> : NSMutableDictionary<KeyType, ObjectType>
@end

@interface NSError (NSErrorBTSKotlinException)
@property (readonly) id _Nullable kotlinException;
@end

__attribute__((swift_name("KotlinNumber")))
@interface BTSNumber : NSNumber
- (instancetype)initWithChar:(char)value __attribute__((unavailable));
- (instancetype)initWithUnsignedChar:(unsigned char)value __attribute__((unavailable));
- (instancetype)initWithShort:(short)value __attribute__((unavailable));
- (instancetype)initWithUnsignedShort:(unsigned short)value __attribute__((unavailable));
- (instancetype)initWithInt:(int)value __attribute__((unavailable));
- (instancetype)initWithUnsignedInt:(unsigned int)value __attribute__((unavailable));
- (instancetype)initWithLong:(long)value __attribute__((unavailable));
- (instancetype)initWithUnsignedLong:(unsigned long)value __attribute__((unavailable));
- (instancetype)initWithLongLong:(long long)value __attribute__((unavailable));
- (instancetype)initWithUnsignedLongLong:(unsigned long long)value __attribute__((unavailable));
- (instancetype)initWithFloat:(float)value __attribute__((unavailable));
- (instancetype)initWithDouble:(double)value __attribute__((unavailable));
- (instancetype)initWithBool:(BOOL)value __attribute__((unavailable));
- (instancetype)initWithInteger:(NSInteger)value __attribute__((unavailable));
- (instancetype)initWithUnsignedInteger:(NSUInteger)value __attribute__((unavailable));
+ (instancetype)numberWithChar:(char)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedChar:(unsigned char)value __attribute__((unavailable));
+ (instancetype)numberWithShort:(short)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedShort:(unsigned short)value __attribute__((unavailable));
+ (instancetype)numberWithInt:(int)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedInt:(unsigned int)value __attribute__((unavailable));
+ (instancetype)numberWithLong:(long)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedLong:(unsigned long)value __attribute__((unavailable));
+ (instancetype)numberWithLongLong:(long long)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedLongLong:(unsigned long long)value __attribute__((unavailable));
+ (instancetype)numberWithFloat:(float)value __attribute__((unavailable));
+ (instancetype)numberWithDouble:(double)value __attribute__((unavailable));
+ (instancetype)numberWithBool:(BOOL)value __attribute__((unavailable));
+ (instancetype)numberWithInteger:(NSInteger)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedInteger:(NSUInteger)value __attribute__((unavailable));
@end

__attribute__((swift_name("KotlinByte")))
@interface BTSByte : BTSNumber
- (instancetype)initWithChar:(char)value;
+ (instancetype)numberWithChar:(char)value;
@end

__attribute__((swift_name("KotlinUByte")))
@interface BTSUByte : BTSNumber
- (instancetype)initWithUnsignedChar:(unsigned char)value;
+ (instancetype)numberWithUnsignedChar:(unsigned char)value;
@end

__attribute__((swift_name("KotlinShort")))
@interface BTSShort : BTSNumber
- (instancetype)initWithShort:(short)value;
+ (instancetype)numberWithShort:(short)value;
@end

__attribute__((swift_name("KotlinUShort")))
@interface BTSUShort : BTSNumber
- (instancetype)initWithUnsignedShort:(unsigned short)value;
+ (instancetype)numberWithUnsignedShort:(unsigned short)value;
@end

__attribute__((swift_name("KotlinInt")))
@interface BTSInt : BTSNumber
- (instancetype)initWithInt:(int)value;
+ (instancetype)numberWithInt:(int)value;
@end

__attribute__((swift_name("KotlinUInt")))
@interface BTSUInt : BTSNumber
- (instancetype)initWithUnsignedInt:(unsigned int)value;
+ (instancetype)numberWithUnsignedInt:(unsigned int)value;
@end

__attribute__((swift_name("KotlinLong")))
@interface BTSLong : BTSNumber
- (instancetype)initWithLongLong:(long long)value;
+ (instancetype)numberWithLongLong:(long long)value;
@end

__attribute__((swift_name("KotlinULong")))
@interface BTSULong : BTSNumber
- (instancetype)initWithUnsignedLongLong:(unsigned long long)value;
+ (instancetype)numberWithUnsignedLongLong:(unsigned long long)value;
@end

__attribute__((swift_name("KotlinFloat")))
@interface BTSFloat : BTSNumber
- (instancetype)initWithFloat:(float)value;
+ (instancetype)numberWithFloat:(float)value;
@end

__attribute__((swift_name("KotlinDouble")))
@interface BTSDouble : BTSNumber
- (instancetype)initWithDouble:(double)value;
+ (instancetype)numberWithDouble:(double)value;
@end

__attribute__((swift_name("KotlinBoolean")))
@interface BTSBoolean : BTSNumber
- (instancetype)initWithBool:(BOOL)value;
+ (instancetype)numberWithBool:(BOOL)value;
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CategorySum")))
@interface BTSCategorySum : BTSBase
- (instancetype)initWithCategoryId:(int64_t)categoryId categoryName:(NSString *)categoryName sum:(int64_t)sum __attribute__((swift_name("init(categoryId:categoryName:sum:)"))) __attribute__((objc_designated_initializer));
- (BTSCategorySum *)doCopyCategoryId:(int64_t)categoryId categoryName:(NSString *)categoryName sum:(int64_t)sum __attribute__((swift_name("doCopy(categoryId:categoryName:sum:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int64_t categoryId __attribute__((swift_name("categoryId")));
@property (readonly) NSString *categoryName __attribute__((swift_name("categoryName")));
@property (readonly) int64_t sum __attribute__((swift_name("sum")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Constants")))
@interface BTSConstants : BTSBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)constants __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) BTSConstants *shared __attribute__((swift_name("shared")));
@property (readonly) NSString *PAYMENT_CARD __attribute__((swift_name("PAYMENT_CARD")));
@property (readonly) NSString *PAYMENT_CASH __attribute__((swift_name("PAYMENT_CASH")));
@property (readonly) NSString *PAYMENT_TRANSFER __attribute__((swift_name("PAYMENT_TRANSFER")));
@property (readonly) NSString *TYPE_EXPENSE __attribute__((swift_name("TYPE_EXPENSE")));
@property (readonly) NSString *TYPE_INCOME __attribute__((swift_name("TYPE_INCOME")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DailySum")))
@interface BTSDailySum : BTSBase
- (instancetype)initWithDate:(NSString *)date income:(int64_t)income expense:(int64_t)expense __attribute__((swift_name("init(date:income:expense:)"))) __attribute__((objc_designated_initializer));
- (BTSDailySum *)doCopyDate:(NSString *)date income:(int64_t)income expense:(int64_t)expense __attribute__((swift_name("doCopy(date:income:expense:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *date __attribute__((swift_name("date")));
@property (readonly) int64_t expense __attribute__((swift_name("expense")));
@property (readonly) int64_t income __attribute__((swift_name("income")));
@property (readonly) int64_t profit __attribute__((swift_name("profit")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DateUtils")))
@interface BTSDateUtils : BTSBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)dateUtils __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) BTSDateUtils *shared __attribute__((swift_name("shared")));
- (NSString *)endOfMonthYearMonth:(NSString *)yearMonth __attribute__((swift_name("endOfMonth(yearMonth:)")));
- (NSString *)formatCurrencyAmount:(int64_t)amount __attribute__((swift_name("formatCurrency(amount:)")));
- (NSString *)startOfMonthYearMonth:(NSString *)yearMonth __attribute__((swift_name("startOfMonth(yearMonth:)")));
- (NSString *)today __attribute__((swift_name("today()")));
- (NSString *)yearMonthDate:(NSString *)date __attribute__((swift_name("yearMonth(date:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("EntryNoteCodec")))
@interface BTSEntryNoteCodec : BTSBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)entryNoteCodec __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) BTSEntryNoteCodec *shared __attribute__((swift_name("shared")));
- (NSString *)mergePaymentMethod:(NSString *)paymentMethod memo:(NSString *)memo __attribute__((swift_name("merge(paymentMethod:memo:)")));
- (BTSKotlinPair<NSString *, NSString *> *)splitNote:(NSString * _Nullable)note defaultPaymentMethod:(NSString *)defaultPaymentMethod __attribute__((swift_name("split(note:defaultPaymentMethod:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Summary")))
@interface BTSSummary : BTSBase
- (instancetype)initWithIncome:(int64_t)income expense:(int64_t)expense __attribute__((swift_name("init(income:expense:)"))) __attribute__((objc_designated_initializer));
- (BTSSummary *)doCopyIncome:(int64_t)income expense:(int64_t)expense __attribute__((swift_name("doCopy(income:expense:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int64_t expense __attribute__((swift_name("expense")));
@property (readonly) int64_t income __attribute__((swift_name("income")));
@property (readonly) int64_t profit __attribute__((swift_name("profit")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinPair")))
@interface BTSKotlinPair<__covariant A, __covariant B> : BTSBase
- (instancetype)initWithFirst:(A _Nullable)first second:(B _Nullable)second __attribute__((swift_name("init(first:second:)"))) __attribute__((objc_designated_initializer));
- (BTSKotlinPair<A, B> *)doCopyFirst:(A _Nullable)first second:(B _Nullable)second __attribute__((swift_name("doCopy(first:second:)")));
- (BOOL)equalsOther:(id _Nullable)other __attribute__((swift_name("equals(other:)")));
- (int32_t)hashCode __attribute__((swift_name("hashCode()")));
- (NSString *)toString __attribute__((swift_name("toString()")));
@property (readonly) A _Nullable first __attribute__((swift_name("first")));
@property (readonly) B _Nullable second __attribute__((swift_name("second")));
@end

#pragma pop_macro("_Nullable_result")
#pragma clang diagnostic pop
NS_ASSUME_NONNULL_END
