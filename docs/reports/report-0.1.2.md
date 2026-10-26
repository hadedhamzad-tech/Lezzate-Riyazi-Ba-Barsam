# گزارش پیاده‌سازی — نسخه 0.1.2 (Stage 1: Question Bank Import & Practice Tab)

## ۱. فهرست دقیق فایل‌های ایجادشده و تغییریافته

### فایل‌های جدید (Created):
- `app/src/main/assets/questions/bank_v1.json`: فایل دارایی (asset) بانک سوالات نمونه با ۳ سوال مشخص‌شده در مشخصات.
- `app/src/main/java/com/example/ui/components/MathText.kt`: کامپوننت متنی MathText برای رندر هوشمند متن‌های آمیخته با فرمول ریاضی بر پایه `PersianUtils.formatMathText`.
- `app/src/main/java/com/example/data/local/QuestionImporter.kt`: کلاس بارگذاری بانک سوالات با بررسی درستی و هش SHA-256 در DataStore، عدم دستکاری سطرهای موجود و درج دسته‌ای ۲۰۰ تایی در `Dispatchers.IO`.
- `app/src/main/java/com/example/ui/screens/PracticeViewModel.kt`: مدیریت حالت تب تمرین (انتخاب پایه، مبحث، درجه دشواری، سوال تصادفی غیرتکراری پشت سر هم و قفل گزینه‌ها).
- `app/src/test/java/com/example/QuestionDaoTest.kt`: تست‌های واحد پرس‌وجوهای فیلترشده و مباحث یکتا به ازای پایه.
- `app/src/test/java/com/example/QuestionImporterTest.kt`: تست‌های واحد واردسازی دوباره و تغییر دارایی بدون اثر منفی روی سطرهای قبلی در پایگاه داده درون‌حافظه‌ای.
- `docs/reports/report-0.1.2.md`: فایل گزارش مستندسازی فاز ۱.

### فایل‌های تغییریافته (Modified):
- `app/build.gradle.kts`: افزایش نسخه به 0.1.2 و versionCode به 3 طبق مانیفست.
- `app/src/main/java/com/example/util/PersianUtils.kt`: گسترش و جایگزینی با کد بررسی جهت و نشانه‌های ریاضی، همراه با افزودن `formatMathText` و متدهای اعتبارسنجی کاراکترها.
- `app/src/main/java/com/example/data/local/QuestionDao.kt`: افزودن پرس‌وجوهای `getTopicsForGrade` و `getFilteredQuestions`.
- `app/src/main/java/com/example/ui/theme/Color.kt`: افزودن رنگ‌های معنایی موفقیت (`LightSuccess`, `DarkSuccess` و کانتینرهای مربوطه).
- `app/src/main/java/com/example/ui/theme/Theme.kt`: تعریف `ExtendedColors` و تزریق `LocalExtendedColors` در پالت تم برای رنگ‌های سبز و تایید پاسخ.
- `app/src/main/java/com/example/ui/screens/PracticeScreen.kt`: پیاده‌سازی کامل صفحه تمرین، فیلترهای پایه/مبحث/دشواری، کارت سوال، کارت‌های گزینه‌ها، نمایش بازخورد بلادرنگ با MathText و دکمه سوال بعدی.
- `app/src/main/java/com/example/ui/navigation/AppNavigation.kt`: اتصال `PracticeViewModel` و ارسال `questionDao` به صفحه تمرین.
- `app/src/main/java/com/example/MainActivity.kt`: اجرای ایمپورت سوالات در پس‌زمینه در زمان راه‌اندازی با `QuestionImporter` بدون مسدودسازی رابط کاربری.
- `app/src/test/java/com/example/AppVersionTest.kt`: به‌روزرسانی نسخه مورد انتظار به 0.1.2 و کد ۳.
- `app/src/test/java/com/example/PersianUtilsTest.kt`: افزودن ۴ آزمون جدید برای صحت رفتار `formatMathText`.

---

## ۲. نتایج تست‌ها (Test Results)

- **تعداد کل تست‌های اجرا شده:** ۱۵ تست
- **تعداد تست‌های موفق (Passed):** ۱۵ تست
- **تعداد تست‌های ناموفق (Failed):** ۰
- **تعداد تست‌های دارای خطا (Errors):** ۰
- **تعداد تست‌های نادیده‌گرفته‌شده (Skipped):** ۰

**ریز نتایج سوئیت‌ها:**
1. `com.example.PersianUtilsTest` (۶ تست - PASS)
   - `testToPersianDigits`: PASS
   - `testWrapMathFormula`: PASS
   - `testPersianOnlyTextIsUnchanged`: PASS
   - `testFormulaInsidePersianTextIsWrappedInLriPdi`: PASS
   - `testFormatMathTextIsIdempotent`: PASS
   - `testFormulaWithIntersectionAndUnionWrappedAsOneRun`: PASS
2. `com.example.QuestionImporterTest` (۲ تست - PASS)
   - `testImportingTwiceGivesSameRowCount`: PASS
   - `testChangedAssetAddsNewRowsWithoutTouchingOldOnes`: PASS
3. `com.example.QuestionDaoTest` (۲ تست - PASS)
   - `testGetTopicsForGrade`: PASS
   - `testGetFilteredQuestions`: PASS
4. `com.example.AppVersionTest` (۱ تست - PASS)
   - `testAppVersionIsNotEmpty`: PASS (صحت 0.1.2 و کد ۳)
5. `com.example.NavRoutesTest` (۱ تست - PASS)
   - `testBottomTabInitialization`: PASS
6. `com.example.SettingsPersistenceTest` (۱ تست - PASS)
   - `testSettingsPersistence`: PASS
7. `com.example.ExampleRobolectricTest` (۱ تست - PASS)
   - `read string from context`: PASS
8. `com.example.ExampleUnitTest` (۱ تست - PASS)
   - `addition_isCorrect`: PASS

---

## ۳. تغییرات در تست‌های موجود و دلایل آن

- `app/src/test/java/com/example/AppVersionTest.kt`:
  - به‌روزرسانی نسخه مورد انتظار از "0.1.1" و کد ۲ به "0.1.2" و کد ۳ بر اساس قانون نسخه‌گذاری مانیفست.
- `app/src/test/java/com/example/PersianUtilsTest.kt`:
  - حفظ تست‌های موجود و اضافه کردن تست‌های پوشش‌دهنده خواص تابع `formatMathText` (متن صرفاً فارسی، احاطه با LRI/PDI، idempotent بودن و نمادهای اشتراک/اجتماع).

---

## ۴. مفروضات و موارد نامطمئن (Assumptions & Uncertainties)

1. **کلید ذخیره هش در DataStore:**
   - هش فایل سوالات در کلید جداگانه `question_bank_sha256` ذخیره شده است تا مستقل از تنظیمات کاربر عمل کند و با تغییر فایل دارایی، مجدداً داده‌ها را بدون تکرار بررسی نماید.
2. **پایه پیش‌فرض صفحه تمرین:**
   - پایه ۱۱ به عنوان مقدار اولیه پیش‌فرض فیلتر در نظر گرفته شده است زیرا سوالات نمونه شامل پایه‌های ۱۱ و ۱۲ می‌باشند.
3. **انتخاب مبحث:**
   - در صورت عدم وجود سوال یا مبحث برای یک پایه، انتخابگر مبحث گزینه «همه مباحث» را نشان می‌دهد.

---

## ۵. مواردی که باید روی گوشی واقعی بررسی شوند

1. **نمایش فرمول‌های ریاضی و جهت‌داری (RTL/LTR):**
   - اطمینان از اینکه عبارات و فرمول‌های ریاضی شامل توان، کسر، رادیکال و پرانتز به صورت چپ‌به‌راست و متن‌های فارسی سوالات به صورت راست‌به‌چپ صحیح دیده می‌شوند.
2. **سرعت و روانی بارگذاری اولیه:**
   - اطمینان از اینکه خواندن فایل دارایی در پس‌زمینه باعث لکنت یا فریز شدن انیمیشن‌های صفحه نخست نمی‌شود.
3. **تغییر فیلترها و رفتار قفل گزینه‌ها:**
   - تست لمس هر گزینه، نمایش رنگ سبز برای گزینه درست، قرمز برای اشتباه و بارگذاری تصادفی سوال بعدی با دکمه «سؤال بعدی».
