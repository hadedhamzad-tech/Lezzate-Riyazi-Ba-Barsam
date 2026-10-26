# گزارش رفع اشکال — نسخه 0.1.3 (Bugfix: KSP NullPointerException on Application.getService)

## علت بروز خطا
خطای زمان کامپایل/اجرا:
`Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException: Cannot invoke "ksp.com.intellij.openapi.application.Application.getService(java.lang.Class)" because the return value of "ksp.com.intellij.openapi.application.ApplicationManager.getApplication()" is null`
این خطا مربوط به باگ شناخته‌شده KSP نسخه ۲.۳.۵ در محیط‌های Headless و ایزوله (گزارش‌شده در مخزن رسمی `google/ksp` به شماره Issue #2763) است که هنگام فراخوانی سرویس‌های کانتینر اینتلی‌جی، به علت عدم وجود نمونه فعال اپلیکیشن اینتلی‌جی، استثنای اشاره‌گر تهی (NPE) پرتاب می‌کرد. این باگ در نسخه ۲.۳.۶ کاتلین سمبل پراسسینگ (KSP) رسماً برطرف شده است. با ارتقای نسخه `googleDevtoolsKsp` از `2.3.5` به `2.3.6` در کاتالوگ نسخه‌ها (`libs.versions.toml`)، این مشکل به طور کامل حل شد.

---

## ۱. فهرست دقیق فایل‌های ایجادشده و تغییریافته

### فایل‌های جدید (Created):
- `docs/reports/report-0.1.3.md`: فایل گزارش مستندسازی نسخه 0.1.3.

### فایل‌های تغییریافته (Modified):
- `gradle/libs.versions.toml`: ارتقای نسخه `googleDevtoolsKsp` از `2.3.5` به `2.3.6` جهت رفع باگ Issue #2763.
- `app/build.gradle.kts`: افزایش نسخه اپلیکیشن به `0.1.3` و `versionCode` به `4` طبق مانیفست پروژه.
- `app/src/test/java/com/example/AppVersionTest.kt`: تطبیق مقادیر assert با نسخه `0.1.3` و کد `4`.

---

## ۲. نتایج تست‌ها (Test Results)

- **تعداد کل تست‌های اجرا شده:** ۱۵ تست
- **تعداد تست‌های موفق (Passed):** ۱۵ تست
- **تعداد تست‌های ناموفق (Failed):** ۰
- **تعداد خطاها (Errors):** ۰
- **تعداد نادیده‌گرفته‌شده (Skipped):** ۰

**ریز نتایج سوئیت‌ها:**
1. `com.example.AppVersionTest` (۱ تست - PASS)
   - `testAppVersionIsNotEmpty`: PASS (بررسی صحت نسخه 0.1.3 و کد ۴)
2. `com.example.ExampleRobolectricTest` (۱ تست - PASS)
   - `read string from context`: PASS
3. `com.example.ExampleUnitTest` (۱ تست - PASS)
   - `addition_isCorrect`: PASS
4. `com.example.NavRoutesTest` (۱ تست - PASS)
   - `testBottomTabInitialization`: PASS
5. `com.example.PersianUtilsTest` (۶ تست - PASS)
   - `testToPersianDigits`: PASS
   - `testFormulaWithIntersectionAndUnionWrappedAsOneRun`: PASS
   - `testFormatMathTextIsIdempotent`: PASS
   - `testWrapMathFormula`: PASS
   - `testFormulaInsidePersianTextIsWrappedInLriPdi`: PASS
   - `testPersianOnlyTextIsUnchanged`: PASS
6. `com.example.QuestionDaoTest` (۲ تست - PASS)
   - `testGetFilteredQuestions`: PASS
   - `testGetTopicsForGrade`: PASS
7. `com.example.QuestionImporterTest` (۲ تست - PASS)
   - `testImportingTwiceGivesSameRowCount`: PASS
   - `testChangedAssetAddsNewRowsWithoutTouchingOldOnes`: PASS
8. `com.example.SettingsPersistenceTest` (۱ تست - PASS)
   - `testSettingsPersistence`: PASS

---

## ۳. تغییرات در تست‌های موجود و دلایل آن

- `app/src/test/java/com/example/AppVersionTest.kt`:
  - به‌روزرسانی مقادیر مورد انتظار از "0.1.2" (کد ۳) به "0.1.3" (کد ۴) به دلیل اعمال قانون نسخه‌گذاری مانیفست.

---

## ۴. مفروضات و موارد نامطمئن (Assumptions & Uncertainties)

1. **سازگاری نسخه KSP 2.3.6:**
   - نسخه KSP 2.3.6 با کامپایلر کاتلین پروژه و پردازشگر Room نسخه ۲.۷.۰ بدون هیچ‌گونه تداخل یا هشداری همگام بوده و با موفقیت بیلد و اجرا شد.

---

## ۵. مواردی که باید روی گوشی واقعی بررسی شوند

1. **تولید کدهای Room و عملکرد دیتابیس:**
   - اطمینان از صحت باز شدن و خواندن پرس‌وجوهای دیتابیس با کلاس‌های تولیدشده توسط نسخه جدید KSP در دستگاه واقعی.
