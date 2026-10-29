# گزارش پیاده‌سازی روزشمار کنکور — نسخه 0.1.13 (versionCode 14)

## ۱. فهرست فایل‌های تغییریافته و جدید
- `app/build.gradle.kts`: ارتقای نگارش برنامه به `versionName = "0.1.13"` و `versionCode = 14`.
- `app/src/main/java/com/example/util/JalaliDate.kt`: افزودن توابع خالص محاسبات روز بر اساس مبدا تقویم میلادی (`gregorianToEpochDay`, `epochDayToGregorian`)، تبدیل معکوس جلالی به میلادی با گام‌زنی قطعی (`jalaliToGregorian`)، اعتبارسنجی تاریخ جلالی (`isValidJalaliDate`)، و محاسبه تعداد روزهای هر ماه جلالی با در نظر گرفتن سال‌های کبیسه (`daysInJalaliMonth`).
- `app/src/main/java/com/example/util/KonkurCountdown.kt` (جدید): شیء مستقل `KonkurCountdown` شامل تاریخ پیش‌فرض (۱۱ تیر ۱۴۰۶)، محاسبه تفاضل روز تقویمی (`daysUntil`)، متن هفته و روز (`weeksAndDaysText`)، خروجی نمایشی کارت با متن‌های دقیق فارسی (`display`)، و تجزیه و سریال‌سازی رشته‌ای با فرمت `yyyy-MM-dd`.
- `app/src/main/java/com/example/data/settings/SettingsRepository.kt`: افزودن کلید ترجیحات `KEY_KONKUR_DATE`، افزودن فیلد `konkurDate: JalaliDate = KonkurCountdown.DEFAULT` به کلاس `UserSettings`، خواندن آن در `userSettingsFlow` و متد ذخیره‌سازی `setKonkurDate(y, m, d)` با فیلتر تاریخ‌های نامعتبر.
- `app/src/main/java/com/example/ui/screens/DashboardScreen.kt`: دریافت پارامتر `konkurDate`، محاسبه روزشمار بر مبنای تاریخ جاری ساعت، و افزودن کارت `dashboard_konkur_card` بین کارت خوش‌آمدگویی و کارت روز هفته با رنگ `tertiaryContainer`، عنوان "روزشمار کنکور 🎡"، متن بزرگ روزها با برچسب آزمون `dashboard_konkur_days_text` و جهت‌بندی ایزوله `LRI/PDI`، پیام امیدبخش، متن هفته‌ها و تاریخ مقصد کنکور.
- `app/src/main/java/com/example/ui/screens/SettingsScreen.kt`: افزودن کارت "تاریخ کنکور" با سه منوی کشویی سال (۱۴۰۵ تا ۱۴۱۰)، ماه (نام‌های ماه‌های جلالی) و روز (۱ تا تعداد روز مجاز ماه) با برچسب‌های `settings_konkur_year`، `settings_konkur_month`، `settings_konkur_day`، کلمپ خودکار روز در صورت تغییر سال یا ماه، دکمه بازنشانی با برچسب `settings_konkur_reset` و فراخوانی کال‌بک `onKonkurDateChange`.
- `app/src/main/java/com/example/ui/navigation/AppNavigation.kt`: اتصال `userSettings.konkurDate` به `DashboardScreen` و کال‌بک `onKonkurDateChange` به `SettingsScreen` متناظر با الگوی `onSoundEnabledChange`.
- `app/src/test/java/com/example/AppVersionTest.kt`: به‌روزرسانی مقادیر مورد انتظار نسخه به "0.1.13" و ۱۴.
- `app/src/test/java/com/example/JalaliConversionTest.kt` (جدید): تست‌های روز مبدا (۱۹۷۰-۰۱-۰۱)، لنگرهای تبدیل جلالی به میلادی، دور رفت و برگشت تمام روزهای سال‌های ۲۰۲۶ تا ۲۰۳۰، قوانین اعتبارسنجی تاریخ‌های جلالی و تعداد روزهای ماه‌ها.
- `app/src/test/java/com/example/KonkurCountdownTest.kt` (جدید): تست تفاضل روزها (۲۴۶ روز، امروز، فردا، گذشته)، متن هفته و روز، پیام‌های نمایشی، تجزیه و سریال‌سازی، و پایداری داده در DataStore.
- `docs/reports/report-0.1.13.md` (جدید): این گزارش.

### خروجی `git diff --stat`:
```text
 app/build.gradle.kts                               |   4 +-
 .../example/data/settings/SettingsRepository.kt    |  20 ++-
 .../com/example/ui/navigation/AppNavigation.kt     |   6 +
 .../java/com/example/ui/screens/DashboardScreen.kt |  61 +++++++
 .../java/com/example/ui/screens/SettingsScreen.kt  | 175 +++++++++++++++++++++
 app/src/main/java/com/example/util/JalaliDate.kt   |  70 +++++++++
 .../main/java/com/example/util/KonkurCountdown.kt  |  74 +++++++++
 app/src/test/java/com/example/AppVersionTest.kt    |   4 +-
 .../test/java/com/example/JalaliConversionTest.kt  |  92 +++++++++++
 .../test/java/com/example/KonkurCountdownTest.kt   | 109 +++++++++++++
 10 files changed, 609 insertions(+), 6 deletions(-)
```

---

## ۲. اقدامات انجام‌شده (What was done)
1. **الگوریتم تقویم و تبدیل تاریخ:** پیاده‌سازی الگوریتم روزهای تقویمی از مبدا سال ۱۹۷۰ میلادی به صورت عدد صحیح خالص بدون `java.time` و بدون `Calendar`، و تبدیل تطبیقی جلالی به میلادی که به‌طور قطعی با `gregorianToJalali` همگام است.
2. **منطق روزشمار کنکور:** محاسبه تفاضل روزهای تقویمی، استخراج تعداد هفته‌ها و روزها، متن‌های انگیزشی متناظر با روزهای باقی‌مانده، و اعتبارسنجی مقادیر در تجزیه/سریال‌سازی.
3. **ذخیره‌سازی پایدار:** ذخیره تاریخ کنکور در DataStore با پیش‌فرض ۱۱ تیر ۱۴۰۶ و فیلتر کردن تاریخ‌های نامعتبر قبل از اعمال تغییر.
4. **رابط کاربری داشبورد و تنظیمات:** درج کارت روزشمار در داشبورد بین کارت خوش‌آمدگویی و روز هفته، و کارت تنظیم تاریخ کنکور با منوهای کشویی و دکمه بازگشت به پیش‌فرض در صفحه تنظیمات.

---

## ۳. مغایرت‌ها با پرامپت (Deviations from prompt)
- **هیچ مغایرتی وجود ندارد.** تمام الزامات اعم از نام فیلدها، برچسب‌های تست، بازه سال‌ها (۱۴۰۵..۱۴۱۰)، رشته‌های دقیق فارسی، مقادیر پیش‌فرض و امضای متدها به‌طور کامل و دقیق پیاده‌سازی شدند.

---

## ۴. نتایج کامپایل و آزمون‌ها (Build & Test Numbers)
- **کامپایل برنامه (`compile_applet`):** موفق (Build succeeded).
- **مجموع آزمون‌های اجرا شده:** ۹۲ تست (۸۲ تست پیشین + ۱۰ تست جدید)
  - `JalaliConversionTest`: ۵ تست (**PASS**)
  - `KonkurCountdownTest`: ۵ تست (**PASS**)
  - سایر تست‌های پیشین: ۸۲ تست (**PASS**)
  - شکست‌ها (Failures): ۰
  - خطاها (Errors): ۰

---

## ۵. موارد تست‌نشده روی دستگاه واقعی (Real-Device Notice)
موارد زیر در تست‌های واحد محلی و محیط ابری قابل سنجش نبوده و باید مستقیماً روی گوشی واقعی بررسی شوند:
1. ظاهر بصری و تناسب فونت کارت جدید روزشمار کنکور در صفحه داشبورد.
2. رفتار لمسی و باز شدن منوهای کشویی روز، ماه و سال در صفحه تنظیمات.
3. ماندگاری تاریخ پس از ری‌استارت و بستن کامل برنامه از بخش Recent Apps.

---

## ۶. چک‌لیست بررسی دستی کاربر روی گوشی (Manual Checklist)
1. **بررسی کارت روزشمار در داشبورد:** باز کردن برنامه و مشاهده کارت روزشمار با عدد حدود ۲۴۶ روز (در تاریخ امروز)، نمایش متن هفته و روز، پیام انگیزشی و تاریخ هدف (۱۱ تیر ۱۴۰۶).
2. **تغییر تاریخ در تنظیمات:** رفتن به صفحه تنظیمات و تغییر ماه به اسفند یا ماه‌های ۳۰ روزه؛ بررسی اینکه روزهای نامعتبر (مانند ۳۰ اسفند در سال غیرکبیسه یا ۳۱ در ماه‌های نیمه دوم) در منوی کشویی انتخاب نشوند یا به روز قبل کلمپ شوند.
3. **دکمه بازگشت به پیش‌فرض:** لمس دکمه «بازگشت به پیشفرض (11 تیر 1406)» و اطمینان از بازگشت مقادیر منوهای کشویی به پیش‌فرض.
4. **پایداری پس از خروج:** تغییر تاریخ، بستن کامل برنامه و بازگشت مجدد جهت اطمینان از حفظ تاریخ ذخیره‌شده.
5. **بررسی در پوسته روشن و تیره:** جابه‌جایی بین حالت تیره و روشن در تنظیمات و بررسی خوانایی و زیبایی رنگ کارت `tertiaryContainer` در هر دو حالت.
