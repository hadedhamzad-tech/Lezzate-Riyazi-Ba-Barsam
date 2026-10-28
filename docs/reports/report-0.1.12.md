# گزارش رفع اشکال صدای برنامه — نسخه 0.1.12 (versionCode 13)

## ۱. فایل‌های تغییریافته (Changed Files)
- `app/src/main/java/com/example/util/sound/AppSoundPlayer.kt`: بازنویسی متد `play` برای تصحیح بررسی وضعیت `AudioTrack.MODE_STATIC` پس از `write()`، جایگزینی لیسنر موقعیت با تاخیر کوروتین (`delay(SoundSynth.durationMs(...) + 150L)`), تغییر خصیصه صوتی به `USAGE_GAME` و `CONTENT_TYPE_SONIFICATION` (دنبال کردن ولوم رسانه)، افزودن `TAG = "AppSound"` و لاگ‌گذاری خطاها بدون کرش برنامه.
- `app/src/main/java/com/example/util/sound/SoundSynth.kt`: افزودن تابع خالص `durationMs(sampleCount: Int): Long = sampleCount * 1000L / SAMPLE_RATE`.
- `app/src/test/java/com/example/SoundSynthTest.kt`: افزودن تست واحد `testDurationMs` برای ارزیابی صحت محاسبه میلی‌ثانیه بر اساس تعداد نمونه‌ها.
- `app/build.gradle.kts`: ارتقای نگارش برنامه به `versionName = "0.1.12"` و `versionCode = 13`.
- `app/src/test/java/com/example/AppVersionTest.kt`: به‌روزرسانی مقادیر مورد انتظار نسخه در تست ("0.1.12" و ۱۳) مطابق مانیفست برای پاس شدن تست‌ها.
- `docs/reports/report-0.1.12.md`: گزارش فعلی.

### فایل‌های ممنوعه که دست‌نخورده باقی ماندند:
- بانک سؤالات (`bank_v1.json`) و فایل‌های روم/پایگاه‌داده دست‌نخورده باقی ماندند.
- نگارش KSP و `libs.versions.toml` بدون تغییر باقی ماندند.
- هیچ کتابخانه یا وابستگی جدیدی اضافه نشد.
- ساختار ظاهری، متن‌ها، فونت‌ها و رنگ‌ها بدون دستکاری حفظ شدند.
- رابط عمومی `AppSoundPlayer` (`play(event: SoundEvent)` و `release()`) بدون تغییر ماند.

---

## ۲. ریشه اشکال و تغییرات انجام‌شده (What was done)
- **ریشه اشکال (Root Cause):** در `AudioTrack` با حالت `MODE_STATIC`، قبل از فراخوانی `write()` مقدار وضعیت ترک برابر `STATE_NO_STATIC_DATA` (مقدار ۲) است و هنوز `STATE_INITIALIZED` نیست. در نسخه 0.1.11 شرط بررسی وضعیت قبل از `write()` اجرا می‌شد که موجب آزادسازی فوری ترک و خروج بدون پخش صدا می‌گردید.
- **اصلاحات:**
  1. بررسی `t.state == AudioTrack.STATE_UNINITIALIZED` قبل از نوشتن داده.
  2. فراخوانی `t.write(pcm, 0, pcm.size)` و سپس بررسی `written == pcm.size` و `t.state == AudioTrack.STATE_INITIALIZED`.
  3. حذف `setNotificationMarkerPosition` و `OnPlaybackPositionUpdateListener` و مدیریت توقف و آزادسازی ترک پس از اتمام پخش با `delay(SoundSynth.durationMs(pcm.size) + 150L)`.
  4. تغییر `AudioAttributes` به `USAGE_GAME` برای تطابق با ولوم رسانه دستگاه.
  5. حفظ دقیق منطق `isSoundEnabled` و `ringerMode == RINGER_MODE_NORMAL`.

---

## ۳. نتیجه کامپایل و تست‌ها (Build & Test Results)
- **کامپایل برنامه (`compile_applet`):** موفق (Build succeeded).
- **اجرای آزمون‌های واحد (`gradle :app:testDebugUnitTest`):**
  - **تعداد کل تست‌ها:** ۸۲ تست
  - **تعداد تست‌های موفق (Passed):** ۸۲ تست
  - **تعداد شکست‌ها و خطاها:** ۰ (همه سبز)

---

## ۴. عدم راستی‌آزمایی خروجی صدا روی دستگاه واقعی (Important Notice)
- **پخش شنیداری و فیزیکی صدا روی گوشی واقعی توسط من راستی‌آزمایی نشده است.**
- در محیط ابری تست، کارت صوتی و خروجی فیزیکی اسپیکر وجود ندارد و تنها درستی محاسبات PCM، عدم کرش، و چرخه ترک صوتی سنجیده شده است.
- ادعایی مبنی بر حل ۱۰۰٪ مشکل روی گوشی واقعی وجود ندارد و شنیده شدن صدای خروجی باید حتماً توسط کاربر روی سخت‌افزار واقعی تست شود.
