# گزارش رفع اشکال — نسخه 0.1.1 (Bugfix: BottomTab Initialization NPE)

## ۱. فهرست دقیق فایل‌های ایجادشده و تغییریافته

### فایل‌های جدید (Created):
- `app/src/test/java/com/example/NavRoutesTest.kt`: تست واحد جدید برای اعتبارسنجی غیرنال بودن تمام تب‌های ناوبری و بررسی ترتیب دقیق ۵ تب از راست به چپ.
- `docs/reports/report-0.1.1.md`: این گزارش مستندسازی.

### فایل‌های تغییریافته (Modified):
- `app/src/main/java/com/example/ui/navigation/NavRoutes.kt`: 
  - تبدیل خاصیت `BottomTab.tabs` به خاصیت دارای گتر (`get() = listOf(...)`) به جای فیلد استاتیک برای جلوگیری از بارگذاری دوری و ایجاد ارجاع null هنگام بارگذاری کلاس‌های شیء سینگلتون فرزند.
  - به‌روزرسانی تعریف اشیاء زیرکلاس‌ها به `data object` برای سازگاری با ساختار مدرن کاتلین.
- `app/build.gradle.kts`: افزایش نسخه طبق مانیفست پروژه (`versionName` به "0.1.1" و `versionCode` به 2).
- `app/src/test/java/com/example/AppVersionTest.kt`: به‌روزرسانی نسخه مورد انتظار از "0.1.0" و کد ۱ به "0.1.1" و کد ۲.

---

## ۲. نتایج تست‌ها (Test Results)

- **تعداد کل تست‌های اجرا شده:** ۷ تست
- **تعداد تست‌های موفق (Passed):** ۷ تست
- **تعداد تست‌های ناموفق (Failed):** ۰
- **تعداد تست‌های دارای خطا (Errors):** ۰
- **تعداد تست‌های نادیده‌گرفته‌شده (Skipped):** ۰

جزئیات تست‌های اجرا شده:
1. `com.example.NavRoutesTest`
   - `testBottomTabInitialization`: PASS (اطمینان از وجود ۵ تب غیرنال و ترتیب صحیح عناوین و مسیرها)
2. `com.example.AppVersionTest`
   - `testAppVersionIsNotEmpty`: PASS (صحت عدم خالی بودن و تطابق versionName با "0.1.1" و versionCode با 2)
3. `com.example.SettingsPersistenceTest`
   - `testSettingsPersistence`: PASS (ماندگاری تنظیمات در DataStore)
4. `com.example.PersianUtilsTest`
   - `testToPersianDigits`: PASS (صحت تبدیل ارقام انگلیسی به فارسی)
   - `testWrapMathFormula`: PASS (صحت ایزوله‌سازی فرمول‌های ریاضی با LRI و PDI)
5. `com.example.ExampleRobolectricTest`
   - `read string from context`: PASS (صحت مقدار نام برنامه در منابع)
6. `com.example.ExampleUnitTest`
   - `addition_isCorrect`: PASS

---

## ۳. تغییرات در تست‌های موجود و دلایل آن

- فایل `app/src/test/java/com/example/AppVersionTest.kt`:
  - مقادیر مورد انتظار در assert از `"0.1.0"` و `1` به `"0.1.1"` و `2` به‌روزرسانی شدند تا با افزایش نسخه ناشی از مانیفست پروژه همگام شوند.

---

## ۴. مفروضات و موارد نامطمئن (Assumptions & Uncertainties)

1. **دلیل بروز خطای زمان اجرا:**
   - خطای `NullPointerException` به علت ارزیابی مقدار فیلد `BottomTab.tabs` در مرحله مقداردهی اولیه استاتیک (`<clinit>`) Companion Object در شرایطی رخ داده بود که نمونه‌های شیء فرزند هنوز بارگذاری کامل نشده بودند. تبدیل آن به getter پویا این مشکل را به طور قطعی برطرف ساخت.

---

## ۵. مواردی که باید روی گوشی واقعی بررسی شوند

1. **اجرای اولیه و عدم کرش:**
   - باز کردن برنامه روی گوشی و اطمینان از بالا آمدن صفحه اول (داشبورد) بدون پرتاب استثناء.
2. **کلیک روی هر یک از ۵ تب ناوبری:**
   - لمس تب‌های «داشبورد»، «تمرین»، «آزمون»، «اشتباهات» و «تحلیل» و اطمینان از تغییر صحیح صفحه به تب انتخابی.
