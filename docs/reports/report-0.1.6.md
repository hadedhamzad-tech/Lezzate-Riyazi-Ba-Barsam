# گزارش پیاده‌سازی — نسخه 0.1.6 (Stage 1.3: Latin digits everywhere + RTL/LTR formula fixes)

## وضعیت پیشرفت اقلام چهارگانه پس از وقفه محدودیت سهمیه (Quota Limit)

در پیاده‌سازی این گام، کار با وقفه محدودیت سهمیه مواجه شد؛ پس از ازسرگیری (Resuming from compaction)، وضعیت پروژه بررسی و تایید شد که هر ۴ مورد با دقت کامل پیاده‌سازی و آزمون شده‌اند:
1. **ارقام لاتین در سراسر رابط کاربری (Latin digits everywhere in UI):** کامل شد (انجام‌شده). تمام ارقام در تراشه‌های پایه، شمارنده فیلتر، شماره سوال و کل چرخه، برچسب گزینه‌ها، گزینه درست در پاسخنامه تشریحی و نسخه برنامه در تنظیمات به ارقام لاتین تبدیل شدند و هیچ فایلی در لایه `ui/` از `toPersianDigits` استفاده نمی‌کند.
2. **اصلاح گروه‌بندی پرانتزها و کروشه‌ها در `formatMathText`:** کامل شد (انجام‌شده). پرانتزها و کروشه‌های نامتوازن باز یا بسته خارج از محدوده فرمول ریاضی باقی می‌مانند و پرانتزهای متوازن درون فرمول حفظ می‌شوند.
3. **فاصله نشکن (U+00A0) در فرمول‌های کوتاه:** کامل شد (انجام‌شده). در فرمول‌های با طول حداکثر ۴۰ نویسه، تمامی فاصله‌ها با فاصله نشکن جایگزین می‌شوند تا از شکست زشت خطوط در فرمول جلوگیری شود؛ فرمول‌های بلندتر فاصله‌های معمولی را حفظ می‌کنند و تابع کاملاً خودتوان (Idempotent) است.
4. **آزمون‌ها و ارتقای نسخه به 0.1.6 (کد ۷):** کامل شد (انجام‌شده). نسخه ارتقا یافت، آزمون نسخه به‌روز شد، و آزمون‌های واحد جدید تمام موارد (a) تا (h) و منع استفاده از ارقام فارسی در UI را با موفقیت پاس کردند.

---

## ۱. فهرست دقیق فایل‌های ایجادشده و تغییریافته

### فایل‌های جدید (Created):
- `docs/reports/report-0.1.6.md`: سند مکتوب گزارش فاز ۱.۳.

### فایل‌های تغییریافته (Modified):
- `app/src/main/java/com/example/util/PersianUtils.kt`:
  - افزودن نویسه فاصله نشکن `\u00A0` به مجموعه نویسه‌های مجاز ریاضی در `isMathAllowedChar`.
  - تعریف شناسایی پرانتزها و کروشه‌های بازشونده (`([{`) و بسته‌شونده (`)]}`).
  - پیاده‌سازی ردگیری توازن پرانتزها (`depth`) در اسکن فرمول‌ها؛ خروج بلادرنگ در صورت مواجهه با پرانتز بسته نامتوازن در `depth == 0` (مانند پرانتز دوم در «`(مساحت 12)`»).
  - عدم ثبت پایان معتبر تا زمان برقراری توازن پرانتزها (`depth == 0`) که سبب باقی‌ماندن پرانتزهای باز نامتوازن در خارج از LRI می‌شود (مانند «`(12 متر`»).
  - جایگزینی فاصله‌های معمولی با نویسه فاصله نشکن (`\u00A0`) برای فرمول‌های با طول حداکثر ۴۰ نویسه جهت جلوگیری از شکستن فرمول بین خطوط در کامپوز، و بازگردانی فاصله‌های معمولی برای فرمول‌های با طول بیش از ۴۰ نویسه.
  - تضمین قطعی خودتوانی (Idempotence) تابع `formatMathText`.
  - نگه‌داشتن متد `toPersianDigits` برای استفاده‌های آتی در صورت نیاز، اما حذف استفاده از آن در لایه UI.
- `app/src/main/java/com/example/ui/screens/PracticeScreen.kt`:
  - تبدیل تراشه‌های پایه تحصیلی به ارقام لاتین (`"پایه $grade"` به‌جای ارقام فارسی).
  - تبدیل شمارنده خط فیلتر به ارقام لاتین (`"${uiState.filterMatchingCount} سؤال در این فیلتر"`).
  - تبدیل نشانگر پیشرفت سوال در کارت به ارقام لاتین (`"سؤال $currentQuestionIndexInRound از $totalQuestionsInRound"`).
  - تبدیل برچسب گزینه‌ها به ارقام لاتین (`(index + 1).toString()` برای نمایش `(1)` تا `(4)`).
  - تبدیل شماره گزینه درست در کادر پاسخنامه تشریحی به رقم لاتین (`(question.correctOptionIndex + 1).toString()`).
- `app/src/main/java/com/example/ui/screens/SettingsScreen.kt`:
  - نمایش شماره نسخه برنامه با ارقام لاتین محصور در پیش‌وند و پس‌وند LRI و PDI (`"${PersianUtils.LRI}${BuildConfig.VERSION_NAME}${PersianUtils.PDI}"`) جهت تضمین چیدمان چپ‌به‌راست و عدم به‌هم‌ریختگی ترتیب ارقام در محیط راست‌به‌چپ (RTL).
- `app/build.gradle.kts`:
  - ارتقای نگارش برنامه به `versionName = "0.1.6"` و `versionCode = 7` طبق قوانین مانیفست.
- `app/src/test/java/com/example/AppVersionTest.kt`:
  - به‌روزرسانی مقادیر مورد انتظار assert به نسخه `"0.1.6"` و کد نسخه `7`.
- `app/src/test/java/com/example/PersianUtilsTest.kt`:
  - به‌روزرسانی آزمون‌های پیشین با فرمت فاصله نشکن جدید.
  - افزودن آزمون حالت (a): بررسی `"(مساحت 12)"` و اطمینان از قرار گرفتن هر دو پرانتز بیرون از محدوده LRI/PDI.
  - افزودن آزمون حالت (b): بررسی `"(12 متر"` و اطمینان از قرار گرفتن پرانتز باز بیرون از محدوده LRI/PDI.
  - افزودن آزمون حالت (c): بررسی `"(g° f)(-3)"` و تجمیع آن در یک اجرای واحد ریاضی با پرانتزهای متوازن.
  - افزودن آزمون حالت (d): بررسی بازه `[-4, -1) ∪ (-1, +∞)` و حفظ یکپارچه ساختار بازه.
  - افزودن آزمون حالت (e): بررسی فرمول با طول <= ۴۰ نویسه (`(a, b)→(a, -b)`) و تایید جایگزینی فاصله‌ها با `\u00A0`.
  - افزودن آزمون حالت (f): بررسی فرمول با طول > ۴۰ نویسه و تایید حفظ فاصله‌های معمولی ASCII.
  - افزودن آزمون حالت (g): بررسی خودتوانی `formatMathText` روی تمام موارد آزمون و سناریوهای ترکیبی.
  - افزودن آزمون حالت (h): بررسی دست‌نخورده ماندن متون صرفاً فارسی.
  - افزودن آزمون جامع `testNoUiFileCallsToPersianDigits`: پیمایش تمام فایل‌های پکیج `ui/` و تایید عدم فراخوانی `toPersianDigits`.

---

## ۲. نتایج تست‌ها و اعداد واقعی (Test Results & Verified Numbers)

- **تعداد کل تست‌های اجرا شده:** ۳۰ تست
- **تعداد تست‌های موفق (Passed):** ۳۰ تست
- **تعداد تست‌های ناموفق (Failed):** ۰
- **تعداد خطاها (Errors):** ۰
- **تعداد نادیده‌گرفته‌شده (Skipped):** ۰

### ریز نتایج مجموعه‌های آزمون:
1. `com.example.PersianUtilsTest` (۱۳ تست - **PASS**)
   - `testToPersianDigits`: PASS
   - `testWrapMathFormula`: PASS
   - `testPersianOnlyTextIsUnchanged`: PASS
   - `testFormulaInsidePersianTextIsWrappedInLriPdi`: PASS
   - `testFormulaWithIntersectionAndUnionWrappedAsOneRun`: PASS
   - `testUnmatchedClosingParenthesisInPersianContextStaysOutside`: PASS (حالت a)
   - `testUnmatchedOpeningParenthesisStaysOutside`: PASS (حالت b)
   - `testBalancedParenthesesArePreservedInSingleMathRun`: PASS (حالت c)
   - `testIntervalExpressionWithBracketsIsPreserved`: PASS (حالت d)
   - `testShortFormulaReplacesSpacesWithNbsp`: PASS (حالت e)
   - `testLongFormulaPreservesAsciiSpaces`: PASS (حالت f)
   - `testFormatMathTextIsIdempotentOnAllCases`: PASS (حالت g)
   - `testNoUiFileCallsToPersianDigits`: PASS (تایید عدم استفاده از ارقام فارسی در لایه UI)
2. `com.example.PracticeRoundSessionTest` (۷ تست - **PASS**)
   - `testEveryQuestionAppearsExactlyOnceInRound`: PASS
   - `testRoundEndsAfterNQuestions`: PASS
   - `testRestartGivesNewRoundWithAllNQuestionsAgain`: PASS
   - `testSameIdSetReemittedDoesNotReset`: PASS
   - `testChangedIdSetOrFilterResets`: PASS
   - `testEdgeCaseNIsOne`: PASS
   - `testEdgeCaseNIsZero`: PASS
3. `com.example.QuestionBankAssetTest` (۱ تست - **PASS**)
   - `testRealAssetFileIntegrity`: PASS (تایید تمامیت و اعتبار ۱۴۲۴ سوال دارایی اصلی)
4. `com.example.AppVersionTest` (۱ تست - **PASS**)
   - `testAppVersionIsNotEmpty`: PASS (تایید نگارش 0.1.6 و کد نسخه ۷)
5. `com.example.QuestionDaoTest` (۲ تست - **PASS**)
   - `testGetFilteredQuestions`: PASS
   - `testGetTopicsForGrade`: PASS
6. `com.example.QuestionImporterTest` (۲ تست - **PASS**)
   - `testImportingTwiceGivesSameRowCount`: PASS
   - `testChangedAssetAddsNewRowsWithoutTouchingOldOnes`: PASS
7. `com.example.SettingsPersistenceTest` (۱ تست - **PASS**)
   - `testSettingsPersistence`: PASS
8. `com.example.NavRoutesTest` (۱ تست - **PASS**)
   - `testBottomTabInitialization`: PASS
9. `com.example.ExampleRobolectricTest` (۱ تست - **PASS**)
   - `read string from context`: PASS
10. `com.example.ExampleUnitTest` (۱ تست - **PASS**)
    - `addition_isCorrect`: PASS

---

## ۳. تغییرات در تست‌های موجود و دلایل آن

1. `app/src/test/java/com/example/AppVersionTest.kt`:
   - مقادیر assert از نسخه "0.1.5" و کد ۶ به نسخه "0.1.6" و کد ۷ به‌روزرسانی شد.
2. `app/src/test/java/com/example/PersianUtilsTest.kt`:
   - آزمون‌های موجود `testFormulaInsidePersianTextIsWrappedInLriPdi` و `testFormulaWithIntersectionAndUnionWrappedAsOneRun` با توجه به قاعده جدید فاصله نشکن در فرمول‌های کوتاه (طول <= ۴۰ نویسه) به‌روزرسانی شدند تا وجود `\u00A0` در فرمول را اعتبارسنجی کنند.

---

## ۴. مفروضات و موارد نامطمئن (Assumptions & Uncertainties)

1. **مدیریت بازه‌های ریاضی با کروشه و پرانتز ناهمگون:**
   - در عباراتی نظیر `[-4, -1)` کروشه بازشونده و پرانتز بسته‌شونده به عنوان بازه متوازن در عمق صفر در نظر گرفته می‌شوند که طبق دستورالعمل صریح تسک به درستی در یک اجرای واحد محصور می‌گردند.
2. **عدم تغییر در بانک داده و اسکیما:**
   - فایل دارایی `bank_v1.json` و اسکیمای دیتابیس روم هیچ تغییری نکردند و نسخه دیتابیس در نگارش ۱ دست‌نخورده باقی ماند.
3. **عدم افزودن کتابخانه‌های جدید:**
   - تمامی تغییرات با متدهای استاندارد کاتلین و Jetpack Compose پیاده‌سازی شدند و فایل کاتالوگ نسخه‌ها (`libs.versions.toml`) تغییر نکرد.

---

## ۵. مواردی که باید روی گوشی واقعی بررسی شوند

1. **نمایش ارقام لاتین در رابط کاربری:**
   - مشاهده تراشه‌های انتخاب پایه تحصیلی (پایه 10، پایه 11، پایه 12).
   - مشاهده خط شمارنده فیلتر (مثلاً «518 سؤال در این فیلتر»).
   - مشاهده برچسب پیشرفت در کارت سوال (مثلاً «سؤال 1 از 518»).
   - مشاهده شماره گزینه‌ها (گزینه 1 تا 4) و شماره گزینه درست در توضیح سوال.
2. **چیدمان نسخه برنامه در صفحه تنظیمات:**
   - بررسی اینکه رشته نسخه برنامه (`0.1.6`) در محیط راست‌به‌چپ دقیقا به صورت 0.1.6 (چپ به راست) خوانده شود و ارقام جابه‌جا یا معکوس نشوند.
3. **شکست خطوط فرمول‌های ریاضی در کامپوز:**
   - بررسی فرمول‌های کوتاه در ابعاد و اندازه‌های مختلف صفحه برای اطمینان از اینکه عباراتی نظیر `(a, b)→(a, -b)` یا `[-4, -1)` به صورت نامناسب و دوپاره در دو خط شکسته نشوند.
4. **نمایش پرانتز در جملات حاوی عبارات و اعداد فارسی:**
   - مشاهده جملاتی شبیه به `(مساحت 12)` و اطمینان از اینکه پرانتزها در دو سوی عبارت فارسی قرار گرفته و به درون رقم نچسبیده‌اند.
