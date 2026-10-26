# گزارش پیاده‌سازی — نسخه 0.1.5 (Stage 1.2: Small UX Fixes)

## ۱. فهرست دقیق فایل‌های ایجادشده و تغییریافته

### فایل‌های جدید (Created):
- `app/src/main/java/com/example/ui/screens/PracticeRoundSession.kt`: کلاس منطق چرخه‌های تمرین به صورت کلاس کاتلین خالص بدون هیچ‌گونه وابستگی به اندروید جهت مدیریت برزدن سوالات، شمارنده، اتمام چرخه، ثبات عدم بازنشانی در بازگسیل همان شناسه‌ها و بازنشانی با تغییر فیلتر.
- `app/src/test/java/com/example/PracticeRoundSessionTest.kt`: آزمون‌های واحد جامع منطق چرخه (بررسی نمایش دقیقاً یک‌باره هر سوال، پایان چرخه در سوال Nام، بازآغاز با برزدن مجدد، مقاومت در برابر بازنشر همان شناسه‌ها، بازنشانی با تغییر فیلتر/شناسه‌ها و حالت‌های مرزی N=1 و N=0).
- `docs/reports/report-0.1.5.md`: سند گزارش مکتوب فاز ۱.۲.

### فایل‌های تغییریافته (Modified):
- `app/src/main/res/values/strings.xml`: تعریف رشته منبع مشترک `coming_soon` با نویسه نیم‌فاصله (`به&#8204;زودی`).
- `app/src/main/java/com/example/util/PersianUtils.kt`: افزودن ثابت سراسری `COMING_SOON_TEXT` با نویسه نیم‌فاصله (U+200C).
- `app/src/main/java/com/example/ui/screens/DashboardScreen.kt`: اتصال متن به رشته منبع مشترک `R.string.coming_soon`.
- `app/src/main/java/com/example/ui/screens/ExamScreen.kt`: اتصال متن به رشته منبع مشترک `R.string.coming_soon`.
- `app/src/main/java/com/example/ui/screens/MistakesScreen.kt`: اتصال متن به رشته منبع مشترک `R.string.coming_soon`.
- `app/src/main/java/com/example/ui/screens/AnalyticsScreen.kt`: اتصال متن به رشته منبع مشترک `R.string.coming_soon`.
- `app/src/main/java/com/example/ui/screens/PracticeViewModel.kt`: اتصال و ادغام `PracticeRoundSession` برای مدیریت چرخه متناهی، شمارنده پیشرفت، بررسی شناسه و مدیریت پایان دور و بازآغاز.
- `app/src/main/java/com/example/ui/screens/PracticeScreen.kt`: افزودن خط نمایش تعداد سوالات فیلتر («<N> سؤال در این فیلتر»)، برچسب پیشرفت در کارت سوال («سؤال <i> از <N>»)، و کارت پایان چرخه با پیام تبریک و دکمه «دوباره از اول».
- `app/build.gradle.kts`: ارتقای نگارش به نسخه `0.1.5` و `versionCode` به `6` طبق مانیفست.
- `app/src/test/java/com/example/AppVersionTest.kt`: تطبیق مقادیر assert با نسخه 0.1.5 و کد ۶.

---

## ۲. نتایج تست‌ها و اعداد واقعی (Test Results & Verified Numbers)

- **تعداد کل تست‌های اجرا شده:** ۲۳ تست
- **تعداد تست‌های موفق (Passed):** ۲۳ تست
- **تعداد تست‌های ناموفق (Failed):** ۰
- **تعداد خطاها (Errors):** ۰
- **تعداد نادیده‌گرفته‌شده (Skipped):** ۰

### ریز نتایج آزمون‌های واحد:
1. `com.example.PracticeRoundSessionTest` (۷ تست - **PASS**)
   - `testEveryQuestionAppearsExactlyOnceInRound`: PASS (بررسی اینکه هر سوال در چرخه دقیقاً یک بار ظاهر می‌شود)
   - `testRoundEndsAfterNQuestions`: PASS (پایان چرخه پس از اتمام سوال Nام)
   - `testRestartGivesNewRoundWithAllNQuestionsAgain`: PASS (شروع مجدد چرخه با برزدن مجدد همه سوالات)
   - `testSameIdSetReemittedDoesNotReset`: PASS (عدم بازنشانی چرخه با انتشار مجدد همان شناسه‌ها از Room Flow)
   - `testChangedIdSetOrFilterResets`: PASS (بازنشانی چرخه با تغییر فیلتر یا شناسه‌ها)
   - `testEdgeCaseNIsOne`: PASS (حالت مرزی تک‌سوالی N = 1)
   - `testEdgeCaseNIsZero`: PASS (حالت مرزی فیلتر خالی N = 0)
2. `com.example.QuestionBankAssetTest` (۱ تست - **PASS**)
   - `testRealAssetFileIntegrity`: PASS (تایید تمامیت ۱۴۲۴ سوال بانک دارایی)
3. `com.example.AppVersionTest` (۱ تست - **PASS**)
   - `testAppVersionIsNotEmpty`: PASS (بررسی صحت نسخه 0.1.5 و کد ۶)
4. `com.example.QuestionDaoTest` (۲ تست - **PASS**)
   - `testGetFilteredQuestions`: PASS
   - `testGetTopicsForGrade`: PASS
5. `com.example.QuestionImporterTest` (۲ تست - **PASS**)
   - `testImportingTwiceGivesSameRowCount`: PASS
   - `testChangedAssetAddsNewRowsWithoutTouchingOldOnes`: PASS
6. `com.example.NavRoutesTest` (۱ تست - **PASS**)
   - `testBottomTabInitialization`: PASS
7. `com.example.PersianUtilsTest` (۶ تست - **PASS**)
   - `testToPersianDigits`: PASS
   - `testWrapMathFormula`: PASS
   - `testPersianOnlyTextIsUnchanged`: PASS
   - `testFormulaInsidePersianTextIsWrappedInLriPdi`: PASS
   - `testFormatMathTextIsIdempotent`: PASS
   - `testFormulaWithIntersectionAndUnionWrappedAsOneRun`: PASS
8. `com.example.SettingsPersistenceTest` (۱ تست - **PASS**)
   - `testSettingsPersistence`: PASS
9. `com.example.ExampleRobolectricTest` (۱ تست - **PASS**)
   - `read string from context`: PASS
10. `com.example.ExampleUnitTest` (۱ تست - **PASS**)
   - `addition_isCorrect`: PASS

---

## ۳. تغییرات در تست‌های موجود و دلایل آن

- `app/src/test/java/com/example/AppVersionTest.kt`:
  - به‌روزرسانی مقادیر مورد انتظار از "0.1.4" (کد ۵) به "0.1.5" (کد ۶) به دلیل ارتقای نگارش مانیفست.

---

## ۴. مفروضات و موارد نامطمئن (Assumptions & Uncertainties)

1. **مدل برزدن سوالات در چرخه‌ها:**
   - برای تضمین تنوع، متد `shuffled()` کاتلین هنگام شروع چرخه یا دکمه «دوباره از اول» فراخوانی می‌شود تا ترتیب سوالات در هر چرخه جدید متفاوت باشد، در حالی که در طول یک چرخه هیچ سوالی تکرار نخواهد شد.
2. **پایداری داده‌ها و عدم تغییر اسکیما:**
   - بانک سوالات و اسکیمای دیتابیس در نسخه ۱ دست‌نخورده باقی ماند و هیچ وابستگی خارجی اضافه نشد.

---

## ۵. مواردی که باید روی گوشی واقعی بررسی شوند

1. **نمایش عبارت «به‌زودی»:**
   - در هر چهار تب (داشبورد، آزمون، اشتباهات، تحلیل)، کلمه با نیم‌فاصله خوانا و بدون چسبیدگی نامناسب دیده شود.
2. **شمارنده و خط تعداد فیلتر در تب تمرین:**
   - نمایش صحیح تعداد کل سوالات فیلتر (مثلاً «۵۱۸ سؤال در این فیلتر») با ارقام فارسی.
   - نمایش صحیح نشانگر «سؤال ۱ از ...» و افزایش گام‌به‌گام آن با هر کلیک روی «سؤال بعدی».
3. **کارت پایان چرخه و دکمه شروع دوباره:**
   - با پاسخ به آخرین سوال فیلتر، کارت تبریک نمایان شده و دکمه «دوباره از اول» چرخه جدیدی با سوالات برزده‌شده آغاز کند.
4. **تغییر فیلترها در میانه چرخه:**
   - با تعویض پایه، مبحث یا درجه دشواری، چرخه فورا ریست شده و سوال ۱ از فیلتر جدید به نمایش درآید.
