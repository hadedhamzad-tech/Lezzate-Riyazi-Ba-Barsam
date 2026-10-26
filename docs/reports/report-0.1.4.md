# گزارش پیاده‌سازی — نسخه 0.1.4 (Stage 1.1: Install Full Question Bank)

## ۱. فهرست دقیق فایل‌های ایجادشده و تغییریافته

### فایل‌های جدید (Created):
- `app/src/test/java/com/example/QuestionBankAssetTest.kt`: تست واحد جدید برای اعتبارسنجی تمامیت فایل دارایی اصلی (`app/src/main/assets/questions/bank_v1.json`) و بررسی پارس بدون خطا، تطابق تعداد دقیق با ۱۴۲۴ سوال، یکتایی تمام شناساگرها، معتبر بودن پایه‌ها، دشواری‌ها، گزینه‌ها و توضیحات.
- `docs/reports/report-0.1.4.md`: سند گزارش مکتوب فاز ۱.۱.

### فایل‌های تغییریافته (Modified):
- `app/src/main/assets/questions/bank_v1.json`: جایگزینی بایت به بایت و دقیق با فایل ارسالی پیوست (`bank_v1_3c1e646c.json`) حاوی ۱۴۲۴ سوال بدون هیچ‌گونه ویرایش یا تغییر دستی (هش SHA-256 فایل: `3c1e646cb51791c8703d037f44ccb6fa36b542bf04fa370a777bc0f14a57d799`).
- `app/build.gradle.kts`: افزایش نسخه به 0.1.4 و versionCode به 5 طبق مانیفست.
- `app/src/test/java/com/example/AppVersionTest.kt`: به‌روزرسانی مقادیر مورد انتظار نسخه به 0.1.4 و کد ۵.

---

## ۲. نتایج تست‌ها و اعداد واقعی (Test Results & Verified Numbers)

- **تعداد کل تست‌های اجرا شده:** ۱۶ تست
- **تعداد تست‌های موفق (Passed):** ۱۶ تست
- **تعداد تست‌های ناموفق (Failed):** ۰
- **تعداد خطاها (Errors):** ۰
- **تعداد نادیده‌گرفته‌شده (Skipped):** ۰

### آمار واقعی استخراج‌شده از بانک سوالات:
- **تعداد کل سوالات موجود در دارایی:** ۱۴۲۴ سوال
- **تعداد شناسه‌های یکتا:** ۱۴۲۴ (بدون شناسه تکراری)
- **توزیع پایه‌های تحصیلی:**
  - پایه ۱۰: ۵۴۳ سوال
  - پایه ۱۱: ۵۱۸ سوال
  - پایه ۱۲: ۳۶۳ سوال
- **توزیع درجه‌های دشواری:**
  - آسان: ۳۴۰ سوال
  - متوسط: ۳۶۵ سوال
  - سخت: ۳۶۶ سوال
  - کنکوری: ۳۵۳ سوال
- **تعداد مباحث متمایز:** ۳۰ مبحث مختلف در ۳ پایه
- **گزینه‌ها و توضیحات:** تمام ۱۴۲۴ سوال دارای ۴ گزینه غیرخالی و توضیح غیرخالی و `correctOptionIndex` در بازه ۰ تا ۳ می‌باشند.

### ریز نتایج آزمون‌های واحد:
1. `com.example.QuestionBankAssetTest` (۱ تست - **PASS**)
   - `testRealAssetFileIntegrity`: PASS (تایید پارس موفق با QuestionImporter، برابری تعداد با ۱۴۲۴، یکتایی شناسه‌ها، معتبر بودن پایه‌ها در {10, 11, 12}، دشواری‌ها در {آسان, متوسط, سخت, کنکوری}، پر بودن گزینه‌ها و توضیحات)
2. `com.example.AppVersionTest` (۱ تست - **PASS**)
   - `testAppVersionIsNotEmpty`: PASS (بررسی نسخه 0.1.4 و کد ۵)
3. `com.example.QuestionDaoTest` (۲ تست - **PASS**)
   - `testGetFilteredQuestions`: PASS
   - `testGetTopicsForGrade`: PASS
4. `com.example.QuestionImporterTest` (۲ تست - **PASS**)
   - `testImportingTwiceGivesSameRowCount`: PASS
   - `testChangedAssetAddsNewRowsWithoutTouchingOldOnes`: PASS
5. `com.example.NavRoutesTest` (۱ تست - **PASS**)
   - `testBottomTabInitialization`: PASS
6. `com.example.PersianUtilsTest` (۶ تست - **PASS**)
   - `testToPersianDigits`: PASS
   - `testWrapMathFormula`: PASS
   - `testPersianOnlyTextIsUnchanged`: PASS
   - `testFormulaInsidePersianTextIsWrappedInLriPdi`: PASS
   - `testFormatMathTextIsIdempotent`: PASS
   - `testFormulaWithIntersectionAndUnionWrappedAsOneRun`: PASS
7. `com.example.SettingsPersistenceTest` (۱ تست - **PASS**)
   - `testSettingsPersistence`: PASS
8. `com.example.ExampleRobolectricTest` (۱ تست - **PASS**)
   - `read string from context`: PASS
9. `com.example.ExampleUnitTest` (۱ تست - **PASS**)
   - `addition_isCorrect`: PASS

---

## ۳. تغییرات در تست‌های موجود و دلایل آن

- `app/src/test/java/com/example/AppVersionTest.kt`:
  - به‌روزرسانی مقادیر assert از "0.1.3" (کد ۴) به "0.1.4" (کد ۵) بر اساس ارتقای نگارش در مانیفست پروژه.

---

## ۴. مفروضات و موارد نامطمئن (Assumptions & Uncertainties)

1. **رفتار ایمپورت در دستگاه‌های قبلی:**
   - با توجه به مکانیزم `INSERT OR IGNORE` بر پایه شناسه پایدار سوالات و تغییر هش SHA-256 دارایی، در نصب‌های تازه دیتابیس ۱۴۲۴ سوال خواهد داشت و در دستگاه‌ها/امولاتورهایی که قبلاً ۳ سوال تستی را داشته‌اند، ۳ سوال قبلی دست‌نخورده باقی مانده و مجموع سوالات به ۱۴۲۴ یا ۱۴۲۷ (در صورت متفاوت بودن شناسه‌ها) خواهد رسید.
2. **عدم دستکاری ساختار دیتابیس:**
   - نسخه پایگاه داده بدون تغییر در نسخه ۱ (version 1) باقی ماند و هیچ میگریشن یا تغییری در اسکیما صورت نگرفت.

---

## ۵. مواردی که باید روی گوشی واقعی بررسی شوند

1. **مشاهده سوالات هر سه پایه و هر چهار سطح دشواری در تب «تمرین»:**
   - تغییر چیپ‌های پایه ۱۰، ۱۱ و ۱۲ و اطمینان از نمایش سوالات متناظر.
   - انتخاب درجه‌های دشواری آسان، متوسط، سخت و کنکوری و بررسی بارگذاری سوالات.
2. **تعداد کل سوالات:**
   - اطمینان از کامل بودن بانک سوالات (۱۴۲۴ سوال).
3. **رندر فرمول‌های ریاضی در متن‌های RTL:**
   - ارزیابی چشمی رندر صحیح کسرها، رادیکال‌ها، توان‌ها، ماتریس‌ها و نمادهای نظریه مجموعه‌ها در متن سوال و گزینه‌ها به صورت چپ‌به‌راست استاندارد در دل پاراگراف‌های فارسی.
