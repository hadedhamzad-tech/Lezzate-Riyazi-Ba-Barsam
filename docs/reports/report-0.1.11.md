# گزارش پیاده‌سازی — نسخه 0.1.11 (Stage 4.0: App Sounds)

## ۱. مبنای اولیه تست‌ها (Baseline Test Count - Step 0)
- **تعداد تست‌های پاس‌شده قبل از هرگونه تغییر:** ۶۹ تست (از ۶۹ تست، ۰ شکست، ۰ خطا)

---

## ۲. فهرست فایل‌های ایجادشده و تغییریافته

### فایل‌های جدید (Created):
- `app/src/main/java/com/example/util/sound/SoundEvent.kt`: تعریف رویدادهای صوتی برنامه شامل `ANSWER_CORRECT`، `ANSWER_WRONG`، `OPTION_TICK`، `EXAM_FINISHED` و `EXAM_TIMEOUT`.
- `app/src/main/java/com/example/util/sound/SoundSynth.kt`: مولد خالص ریاضی صدا (PCM ۱۶ بیتی مونو با نرخ ۲۲,۰۵۰ هرتز)، اعمال فید ۸ میلی‌ثانیه‌ای ابتدا و انتها برای جلوگیری از کلیک، محدودسازی دامنه اوج به حدود ۰.۳۵ مقیاس، نگاشت نوت‌ها به رویدادها، و تابع منطقی `shouldPlay(soundEnabled, ringerModeNormal)`.
- `app/src/main/java/com/example/util/sound/AppSoundPlayer.kt`: پخش‌کننده صوتی اندروید با `AudioTrack` در حالت `MODE_STATIC` و ویژگی‌های صوتی اعلان (`USAGE_ASSISTANCE_SONIFICATION`) روی دیسپچر پس‌زمینه بدون بلاک کردن UI، بررسی فوری وضعیت تنظیمات صدا و حالت زنگ گوشی (`RINGER_MODE_NORMAL`) قبل از پخش، کش تنبل بافرها، و مدیریت امن با `try/catch`.
- `app/src/test/java/com/example/SoundSynthTest.kt`: ۸ تست واحد جامع برای مولد صوتی شامل تطابق طول بافر با مدت زمان، بررسی دامنه اوج (کمتر از ۰.۴ مقدار بیشینه)، بررسی فید ابتدا و انتها (نمونه‌های صفر)، بررسی عدم سکوت محض در اصوات، بررسی وجود سکوت ۶۰ میلی‌ثانیه‌ای در تایم‌اوت، قطعی بودن تولید (ورودی یکسان = خروجی یکسان)، و جدول درستی `shouldPlay`.
- `app/src/test/java/com/example/SoundEventViewModelTest.kt`: ۴ تست واحد برای رفتار تک‌شات رویدادها در ویومدل‌های تمرین و آزمون (پخش تک‌باره، عدم تکرار پس از قفل پاسخ، تیک انتخاب و لغو انتخاب در آزمون، پایان دستی و تایم‌اوت).
- `docs/reports/report-0.1.11.md`: این گزارش مستند.

### فایل‌های تغییریافته (Modified):
- `app/src/main/java/com/example/data/settings/SettingsRepository.kt`: افزودن متغیر فرار `@Volatile var isSoundEnabled` و همگام‌سازی سریع آن با جریان تنظیمات و متد `setSoundEnabled` جهت اعمال بلادرنگ کلید تغییر صدا.
- `app/src/main/java/com/example/ui/screens/PracticeViewModel.kt`: افزودن `soundEvents: SharedFlow<SoundEvent>` و انتشار رویداد `ANSWER_CORRECT` یا `ANSWER_WRONG` در متد `selectOption` هنگام قفل شدن پاسخ.
- `app/src/main/java/com/example/ui/screens/PracticeScreen.kt`: دریافت `soundPlayer` و اشتراک روی `viewModel.soundEvents` داخل `LaunchedEffect` بدون پخش مجدد در بازسازی کامپوز.
- `app/src/main/java/com/example/ui/screens/ExamViewModel.kt`: افزودن `soundEvents: SharedFlow<SoundEvent>` و انتشار رویدادهای `OPTION_TICK` (انتخاب/حذف گزینه)، `EXAM_FINISHED` (پایان دستی) و `EXAM_TIMEOUT` (اتمام زمان).
- `app/src/main/java/com/example/ui/screens/ExamScreen.kt`: دریافت `soundPlayer` و اشتراک روی `viewModel.soundEvents` داخل `LaunchedEffect`.
- `app/src/main/java/com/example/ui/navigation/AppNavigation.kt`: دریافت شیء `soundPlayer` و ارسال آن به عنوان پارامتر به صفحات `PracticeScreen` و `ExamScreen`.
- `app/src/main/java/com/example/MainActivity.kt`: ایجاد نمونه یکتای `AppSoundPlayer` در `onCreate`، ارسال به ناوبری، و آزادسازی کامل آن در `onDestroy` بدون نشت کانتکست.
- `app/build.gradle.kts`: ارتقای نگارش برنامه به `versionName = "0.1.11"` و `versionCode = 12`.
- `app/src/test/java/com/example/AppVersionTest.kt`: به‌روزرسانی مقادیر مورد انتظار assert به "0.1.11" و ۱۲.

---

## ۳. فهرست فایل‌های ممنوعه که دست‌نخورده باقی ماندند (Explicitly Untouched)

مطابق مانیفست و دستور صریح تسک، هیچ‌یک از فایل‌های زیر لمس نشدند:
1. `app/src/main/java/com/example/data/local/QuestionImporter.kt` (دست‌نخورده)
2. فایل‌های پایگاه داده و روم: `AppDatabase.kt`، `QuestionDao.kt`، `QuestionEntity.kt` (اسکیما در نسخه ۱ بدون تغییر)
3. `gradle/libs.versions.toml` (نگارش KSP 2.3.6 دست‌نخورده، هیچ کتابخانه جدیدی اضافه نشد)
4. `app/src/main/assets/questions/bank_v1.json` (۱۴۲۴ سؤال دست‌نخورده، هیچ فایل صوتی به assets افزوده نشد)
5. لاجیک نشست، نمره‌دهی و مرور آزمون: `ExamSession.kt`، `ExamScoring.kt`، `ExamReview.kt`
6. `PracticeRoundSession.kt`
7. `PersianUtils.kt`
8. فایل‌های پوسته و استایل: `Theme.kt`، `Color.kt`، `Type.kt`
9. هیچ متن، چیدمان، کامپوننت، یا رنگی در هیچ صفحه‌ای تغییر نیافت.

---

## ۴. نتایج نهایی تست‌ها (Exact Test Results)

- **تعداد کل تست‌های اجرا شده:** ۸۱ تست
- **تعداد تست‌های موفق (Passed):** ۸۱ تست
- **تعداد تست‌های ناموفق (Failed):** ۰
- **تعداد خطاها (Errors):** ۰
- **تعداد نادیده‌گرفته‌شده (Skipped):** ۰

### تفکیک مجموعه‌های آزمون:
- `com.example.AppVersionTest`: ۱ تست (**PASS**)
- `com.example.DashboardTest`: ۷ تست (**PASS**)
- `com.example.ExamResultFormatterTest`: ۲ تست (**PASS**)
- `com.example.ExamReviewTest`: ۸ تست (**PASS**)
- `com.example.ExamScoringTest`: ۸ تست (**PASS**)
- `com.example.ExamSessionTest`: ۱۱ تست (**PASS**)
- `com.example.ExampleRobolectricTest`: ۱ تست (**PASS**)
- `com.example.ExampleUnitTest`: ۱ تست (**PASS**)
- `com.example.NavRoutesTest`: ۱ تست (**PASS**)
- `com.example.PersianHalfSpaceTest`: ۳ تست (**PASS**)
- `com.example.PersianUtilsTest`: ۱۳ تست (**PASS**)
- `com.example.PracticeRoundSessionTest`: ۷ تست (**PASS**)
- `com.example.QuestionBankAssetTest`: ۱ تست (**PASS**)
- `com.example.QuestionDaoTest`: ۲ تست (**PASS**)
- `com.example.QuestionImporterTest`: ۲ تست (**PASS**)
- `com.example.SettingsPersistenceTest`: ۱ تست (**PASS**)
- `com.example.SoundEventViewModelTest`: ۴ تست جدید (**PASS**)
- `com.example.SoundSynthTest`: ۸ تست جدید (**PASS**)

---

## ۵. تغییرات در تست‌های موجود و دلایل آن

- `app/src/test/java/com/example/AppVersionTest.kt`:
  - به‌روزرسانی مقادیر مورد انتظار assert از `"0.1.10"` (کد ۱۱) به `"0.1.11"` (کد ۱۲) به دلیل ارتقای نگارش برنامه طبق مانیفست.

---

## ۶. موارد مشاهده‌شده ولی تغییرنیافته (Observed but not changed)

1. `app/src/main/java/com/example/ui/screens/PracticeScreen.kt`:
   - خط ۲۴۱: `"در حال آمادهسازی بانک سؤال..."` (کلمه مرکب آماده‌سازی).
   - خط ۳۰۴: `"همهی سؤالهای این فیلتر را دیدی 🎉"` (شکل‌های مجاز/نرمال بدون تغییر).
   - خط ۵۴۲: `"توضیح راهحل"` (کلمه مرکب راه‌حل).
2. هشدارهای مربوط به استفاده از آیکون‌های متریال (مانند `AutoMirrored` برای آیکون‌های جهت‌دار در ناوبری و صفحات).
3. عدم وجود تنظیم بلندی صدای اختصاصی در صفحه تنظیمات (در حال حاضر کنترل حجم صدا وابسته به صدای رسانه/اعلان خود سیستم‌عامل است).

---

## ۷. مفروضات و موارد نامطمئن (Assumptions & Uncertainties)

1. **عدم امکان راستی‌آزمایی خروجی واقعی اسپیکر در تست‌های واحد:**
   - تست‌های واحد JVM به طور کامل ساختار داده‌های PCM، محاسبات سینوسی، فیدها، دامنه‌ها، جدول درستی `shouldPlay` و انتشار رویدادهای ویومدل را اعتبارسنجی می‌کنند؛ با این وجود، نحوه تعامل لایه HAL صوتی اندروید، درایور سخت‌افزاری و صدای واقعی اسپیکر تلفن همراه در تست واحد قابل شنیدن یا سنجش مستقیم فیزیکی نیست و باید روی تلفن همراه واقعی بررسی شود.
2. **چرخه حیات اشیاء AudioTrack در حالت MODE_STATIC:**
   - برای هر صدای پخش‌شده، شیء `AudioTrack` با لیسنر اتمام مارکر (`onMarkerReached`) به صورت خودکار متوقف و آزاد می‌شود و در صورت تخریب اکتیویتی نیز تمامی ترک‌های فعال پاک‌سازی می‌شوند تا نشت منابع صوتی رخ ندهد.

---

## ۸. مواردی که باید روی گوشی واقعی بررسی شوند (Real Phone Checklist)

1. **بررسی صدای تب تمرین (Practice Tab):**
   - انتخاب گزینه درست: شنیده شدن ملودی بالارونده کوتاه (۶۵۹ و ۸۸۰ هرتز)، ملایم و دقیقاً یک‌بار.
   - انتخاب گزینه غلط: شنیده شدن ملودی آرام فرودآینده (۳۳۰ و ۲۴۷ هرتز) با ولوم کمتر، دقیقاً یک‌بار.
   - لمس مجدد گزینه‌ها پس از قفل شدن: عدم پخش هرگونه صدای مجدد.
2. **بررسی صدای تب آزمون (Exam Tab):**
   - لمس هر گزینه برای انتخاب: شنیده شدن تیک بسیار کوتاه و یکسان (۸۰۰ هرتز) برای تمام گزینه‌ها (بدون لو رفتن پاسخ درست).
   - لمس گزینه انتخاب‌شده برای لغو انتخاب (Clear): شنیده شدن همان صدای تیک کوتاه.
   - اتمام دستی آزمون و تأیید دیالوگ: شنیده شدن ملودی پایان آزمون (چهار نوت بالارونده).
   - اتمام زمان آزمون (Timeout): شنیده شدن صدای بیپ دوگانه با فاصله سکوت بین آن.
3. **بررسی کلید فعال/غیرفعال صدا در تنظیمات:**
   - خاموش کردن کلید صدا در صفحه تنظیمات: قطع فوری تمام اصوات در تمرین و آزمون.
   - روشن کردن مجدد کلید: بازگشت فوری اصوات بدون نیاز به بستن برنامه.
4. **بررسی حالت بی‌صدا و ویبره (Silent & Vibrate):**
   - قرار دادن گوشی در حالت سایلنت یا ویبره: عدم پخش هیچ‌گونه صدایی از برنامه.
5. **بررسی عدم پخش تکراری در چرخش یا جابه‌جایی تب‌ها:**
   - چرخش صفحه (Orientation change) در حین آزمون یا پس از پاسخ‌دهی در تمرین: عدم بازپخش صدا.
   - جابه‌جایی بین تب‌های نوار پایینی و بازگشت به صفحه: عدم پخش صدای ناخواسته.
6. **عدم وجود صدا در سایر بخش‌ها:**
   - لمس المان‌ها در داشبورد، تنظیمات، ناوبری و صفحه مرور آزمون کاملاً بی‌صدا باشند.
7. **پاسخ‌دهی سریع و پایداری:**
   - لمس سریع گزینه‌ها نباید هیچ لگ، تاخیر یا کرشی در برنامه ایجاد کند.
