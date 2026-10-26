# گزارش پیاده‌سازی — نسخه 0.1.0 (Stage 0: Skeleton)

## ۱. فهرست دقیق فایل‌های ایجادشده و تغییریافته

### فایل‌های جدید (Created):
- `app/src/main/java/com/example/util/PersianUtils.kt`: توابع کمکی تبدیل ارقام به فارسی و جداسازی فرمول‌های ریاضی با نویسه‌های کنترلی ایزوله جهت‌داری U+2066 (LRI) و U+2069 (PDI).
- `app/src/main/java/com/example/data/settings/ThemeMode.kt`: تعریف نوع شمارشی حالت‌های پوسته (پیرو سیستم، روشن، تاریک).
- `app/src/main/java/com/example/data/settings/SettingsRepository.kt`: لایه دسترسی و ذخیره‌سازی ترجیحات کاربر (حالت پوسته، وضعیت صدا) با استفاده از Jetpack DataStore Preferences.
- `app/src/main/java/com/example/data/local/QuestionEntity.kt`: انتیتی Room نسخه ۱ برای سوالات کنکور با فیلدهای مشخص‌شده طبق مانیفست.
- `app/src/main/java/com/example/data/local/QuestionDao.kt`: اینترفیس دسترسی به داده‌ها با راهبرد OnConflictStrategy.IGNORE جهت حفظ داده‌های کاربر هنگام ورود داده.
- `app/src/main/java/com/example/data/local/AppDatabase.kt`: پایگاه داده Room نسخه ۱ با فعال‌سازی exportSchema = true.
- `app/src/main/java/com/example/ui/navigation/NavRoutes.kt`: تعریف مسیرهای ۵ تب ناوبری پایینی و صفحه تنظیمات همراه با آیکون‌ها و شناسه تست‌ها.
- `app/src/main/java/com/example/ui/navigation/AppNavigation.kt`: کنترلر ناوبری Compose با چینش راست‌به‌چپ (RTL)، نوار بالا و نوار پایین.
- `app/src/main/java/com/example/ui/components/AppTopBar.kt`: نوار بالایی اپلیکیشن با عنوان صفحه، دکمه ورود به تنظیمات و دکمه بازگشت در صفحه تنظیمات.
- `app/src/main/java/com/example/ui/components/AppBottomBar.kt`: نوار ناوبری پایینی برای ۵ تب اصلی از راست به چپ.
- `app/src/main/java/com/example/ui/screens/DashboardScreen.kt`: صفحه تب «داشبورد» با عنوان و متن «بهزودی».
- `app/src/main/java/com/example/ui/screens/PracticeScreen.kt`: صفحه تب «تمرین» با عنوان و متن «بهزودی».
- `app/src/main/java/com/example/ui/screens/ExamScreen.kt`: صفحه تب «آزمون» با عنوان و متن «بهزودی».
- `app/src/main/java/com/example/ui/screens/MistakesScreen.kt`: صفحه تب «اشتباهات» با عنوان و متن «بهزودی».
- `app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt`: صفحه تب «تحلیل» با عنوان و متن «بهزودی».
- `app/src/main/java/com/example/ui/screens/SettingsScreen.kt`: صفحه تنظیمات شامل انتخاب پوسته (سیستم/روشن/تاریک)، سوئیچ فعال/غیرفعال‌سازی صدا، و نمایش نسخه برنامه از BuildConfig.
- `app/src/test/java/com/example/SettingsPersistenceTest.kt`: تست واحد مانا بودن تنظیمات پوسته و صدا در DataStore با Robolectric.
- `app/src/test/java/com/example/AppVersionTest.kt`: تست واحد غیرخالی بودن و صحت نسخه برنامه (0.1.0 و کد ۱) در BuildConfig.
- `app/src/test/java/com/example/PersianUtilsTest.kt`: تست واحد ارقام فارسی و ایزوله‌سازی فرمول‌های ریاضی.
- `docs/reports/report-0.1.0.md`: این گزارش مستندسازی.

### فایل‌های تغییریافته (Modified):
- `metadata.json`: تنظیم نام پلتفرم به «لذت ریاضی با برسام» و توضیحات مرتبط.
- `settings.gradle.kts`: به‌روزرسانی `rootProject.name` به "JoyOfMathBarsam".
- `app/build.gradle.kts`: تنظیم versionName روی 0.1.0، versionCode روی 1، فعال‌سازی وابستگی‌های Navigation Compose و DataStore، پیکربندی ksp room.schemaLocation و غیرفعال‌سازی وابستگی‌های شبکه/هوش‌مصنوعی اضافه برای سبک‌سازی بیلد.
- `app/src/main/res/values/strings.xml`: تغییر `app_name` به «لذت ریاضی با برسام».
- `app/src/main/res/drawable/ic_launcher_background.xml`: گرادیان سبز یشمی با بافت مشبک ریاضی برای آیکون تطبیقی لانچر.
- `app/src/main/res/drawable/ic_launcher_foreground.xml`: نماد ریاضی رادیکال و متغیر به رنگ کهربایی و سفید در محدوده امن ۶۶dp.
- `app/src/main/res/mipmap-*/ic_launcher.png` و `ic_launcher_round.png`: فایل‌های رستر جایگزین آیکون پیش‌فرض اندروید و حذف وب‌پی‌های پیش‌فرض تمپلیت.
- `app/src/main/java/com/example/ui/theme/Color.kt`: تعریف رنگ‌های معنایی سبز یشمی (Jade Green) به عنوان رنگ اصلی و کهربایی (Amber) به عنوان رنگ مکمل.
- `app/src/main/java/com/example/ui/theme/Theme.kt`: پیاده‌سازی AppTheme با پشتیبانی از تم‌های روشن/تاریک و کنترل از طریق تنظیمات کاربر.
- `app/src/main/java/com/example/MainActivity.kt`: اتصال ناوبری، DataStore و مدیریت حالت تم به صورت Edge-to-Edge.
- `app/src/test/java/com/example/ExampleRobolectricTest.kt`: به‌روزرسانی نام برنامه مورد انتظار در تست context به «لذت ریاضی با برسام».

---

## ۲. نتایج تست‌ها (Test Results)

- **تعداد کل تست‌های اجرا شده:** ۶ تست
- **تعداد تست‌های موفق (Passed):** ۶ تست
- **تعداد تست‌های ناموفق (Failed):** ۰
- **تعداد تست‌های دارای خطا (Errors):** ۰
- **تعداد تست‌های نادیده‌گرفته‌شده (Skipped):** ۰

جزئیات سوئیت‌های تست:
1. `com.example.SettingsPersistenceTest`
   - `testSettingsPersistence`: PASS (ذخیره و بازیابی تغییر تم به تاریک و فعال/غیرفعال‌سازی صدا)
2. `com.example.AppVersionTest`
   - `testAppVersionIsNotEmpty`: PASS (صحت عدم خالی بودن versionName و برابری با 0.1.0 و کد ۱)
3. `com.example.PersianUtilsTest`
   - `testToPersianDigits`: PASS (صحت تبدیل ارقام انگلیسی به فارسی)
   - `testWrapMathFormula`: PASS (صحت احاطه فرمول‌ها با LRI و PDI)
4. `com.example.ExampleRobolectricTest`
   - `read string from context`: PASS (صحت خواندن عنوان اپلیکیشن از منابع اندروید)
5. `com.example.ExampleUnitTest`
   - `addition_isCorrect`: PASS

---

## ۳. تغییرات در تست‌های موجود و دلایل آن

- فایل `app/src/test/java/com/example/ExampleRobolectricTest.kt`:
  - خط ۱۹ رشته مورد انتظار تست از `"My Application"` به `"لذت ریاضی با برسام"` تغییر یافت، زیرا طبق دستورالعمل نام نمایشی اپلیکیشن تغییر کرده و تست قبلی با عنوان تمپلیت مقایسه انجام می‌داد.

---

## ۴. مفروضات و موارد نامطمئن (Assumptions & Uncertainties)

1. **انتیتی QuestionEntity:**
   - فیلد `difficulty` به عنوان رشته متنی (`String`) در نظر گرفته شد تا مقادیر مختلف طبقه‌بندی دشواری را پوشش دهد.
   - فیلد `grade` به صورت عدد صحیح (`Int`) برای پایه‌های ۱۰، ۱۱، ۱۲ منظور شد.
2. **شکل و فونت پیش‌فرض:**
   - در فاز اسکلت اولیه (Stage 0)، تایپوگرافی Material 3 پیش‌فرض سیستم به کار گرفته شده است و هیچ فونت خارجی اضافه نشده است.
3. **پایگاه داده Room در بیلد اولیه:**
   - پایگاه داده برای ساخت اولیه آماده شده و فایل اسکیما در دایرکتوری `app/schemas` نگاشت شده است؛ داده‌ای تا دستور بعدی وارد نشده است.

---

## ۵. مواردی که باید روی گوشی واقعی بررسی شوند

1. **جهت‌داری راست‌به‌چپ (RTL):**
   - ترتیب تب‌های پایین صفحه از راست به چپ (داشبورد، تمرین، آزمون، اشتباهات، تحلیل) بررسی شود.
   - وضعیت باز شدن نوار بالا و انیمیشن بازگشت دکمه Back در دستگاه‌های با حالت‌های مختلف سیستم‌عامل بررسی شود.
2. **عملکرد پوسته تاریک/روشن:**
   - تغییر حالت پوسته در صفحه تنظیمات و اعمال بلادرنگ آن روی رنگ‌های زمینه و کارت‌ها بررسی شود.
   - هماهنگی با تم دارک خودکار گوشی در حالت «پیرو سیستم» تست شود.
3. **وضوح و کادربندی آیکون لانچر:**
   - نمایش آیکون رادیکال و بافت یشمی روی لانچرهای مختلف (مثل One UI, Pixel Launcher, MIUI) به صورت گرد و مربعی بررسی شود.
