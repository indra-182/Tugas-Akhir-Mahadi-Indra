# Kop Laporan Hanya pada Halaman Pertama Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Render the report header only on the first page while allowing later preview/PDF pages to use the larger content area without skipping or duplicating rows.

**Architecture:** Add a pure `PaginasiLaporan` utility that produces page ranges for a first-page capacity and a continuation-page capacity. `LaporanPanel.CetakLaporanPrintable` will use those ranges for both ordinary table reports and TOPSIS sections, and will call the existing header renderer only for the first range. Preview and PDF remain on the same `Printable` path.

**Tech Stack:** Java 8, Java Swing/AWT `Printable`, JUnit 4, Ant/NetBeans build.

## Global Constraints

- Cover all five report types: ranking, penilaian, karyawan, tren kinerja karyawan, and evaluasi TOPSIS.
- Keep preview and PDF on the same `CetakLaporanPrintable` renderer.
- Keep table headers on every table page and the signature only on the final page.
- Do not change data sources, DAO behavior, database schema, report content, or dependencies.
- Use English for Java identifiers, comments, and commit messages; preserve the repository's existing Indonesian UI text.

## File Map

- Create `src/com/mahadi/indivaragroup/util/PaginasiLaporan.java`: pure page-range calculation for one table or multiple TOPSIS sections.
- Create `test/com/mahadi/indivaragroup/util/PaginasiLaporanTest.java`: unit tests for capacity changes, contiguous ranges, section transitions, and invalid inputs.
- Create `test/com/mahadi/indivaragroup/ui/LaporanPanelTest.java`: regression test for the first-page-only header rule without constructing the database-backed Swing panel.
- Modify `src/com/mahadi/indivaragroup/ui/LaporanPanel.java`: use page-aware ranges and render the existing header only for the first range.
- Modify `build.xml`: add the two new test classes to `academic.test.classes` so `ant test` executes them.

---

### Task 1: Add test-first page-range calculation

**Files:**
- Create: `test/com/mahadi/indivaragroup/util/PaginasiLaporanTest.java`
- Modify: `build.xml:6-13` (`academic.test.classes`)

**Interfaces:**
- The tests define the required API for `PaginasiLaporan.buat(...)`, `PaginasiLaporan.buatBagian(...)`, and `PaginasiLaporan.RentangHalaman`.

- [ ] **Step 1: Write the failing tests**

Create the test class in package `com.mahadi.indivaragroup.util` with these behaviors:

```java
@Test
public void usesContinuationCapacityWithoutSkippingRows() {
    List<PaginasiLaporan.RentangHalaman> halaman = PaginasiLaporan.buat(10, 3, 4);

    assertEquals(3, halaman.size());
    assertEquals(0, halaman.get(0).getBarisMulai());
    assertEquals(3, halaman.get(0).getBarisAkhir());
    assertTrue(halaman.get(0).isHalamanPertama());
    assertEquals(3, halaman.get(1).getBarisMulai());
    assertEquals(7, halaman.get(1).getBarisAkhir());
    assertFalse(halaman.get(1).isHalamanPertama());
    assertEquals(7, halaman.get(2).getBarisMulai());
    assertEquals(10, halaman.get(2).getBarisAkhir());
}

@Test
public void paginatesTopsisSectionsContiguously() {
    List<PaginasiLaporan.RentangHalaman> halaman = PaginasiLaporan.buatBagian(
            Arrays.asList(4, 3), 2, 3);

    assertEquals(3, halaman.size());
    assertEquals(0, halaman.get(0).getIndeksBagian());
    assertEquals(0, halaman.get(0).getBarisMulai());
    assertEquals(2, halaman.get(0).getBarisAkhir());
    assertEquals(0, halaman.get(1).getIndeksBagian());
    assertEquals(2, halaman.get(1).getBarisMulai());
    assertEquals(4, halaman.get(1).getBarisAkhir());
    assertEquals(1, halaman.get(2).getIndeksBagian());
    assertEquals(0, halaman.get(2).getBarisMulai());
    assertEquals(3, halaman.get(2).getBarisAkhir());
    assertFalse(halaman.get(2).isHalamanPertama());
}

@Test
public void returnsNoRangesForEmptyRows() {
    assertTrue(PaginasiLaporan.buat(0, 3, 4).isEmpty());
}

@Test(expected = IllegalArgumentException.class)
public void rejectsNonPositiveCapacity() {
    PaginasiLaporan.buat(1, 0, 4);
}
```

Use JUnit 4 assertions already used by this repository (`assertEquals`, `assertTrue`, `assertFalse`). The test must inspect actual range values, not only the number of pages.

Add `com.mahadi.indivaragroup.util.PaginasiLaporanTest` to the `academic.test.classes` property in `build.xml`.

- [ ] **Step 2: Run the test to verify it fails**

Run:

```bash
ant test
```

Expected result: test compilation fails because `PaginasiLaporan` and its nested `RentangHalaman` API do not exist yet. Do not proceed with production implementation until this is the observed failure.

- [ ] **Step 3: Commit the red test**

```bash
git add build.xml test/com/mahadi/indivaragroup/util/PaginasiLaporanTest.java
git commit -m "test: define report pagination ranges"
```

### Task 2: Implement the pure pagination utility

**Files:**
- Create: `src/com/mahadi/indivaragroup/util/PaginasiLaporan.java`
- Test: `test/com/mahadi/indivaragroup/util/PaginasiLaporanTest.java`

**Interfaces:**
- `public static List<RentangHalaman> buat(int jumlahBaris, int kapasitasHalamanPertama, int kapasitasHalamanBerikutnya)` returns ranges for one table with section index `0`.
- `public static List<RentangHalaman> buatBagian(List<Integer> jumlahBarisBagian, int kapasitasHalamanPertama, int kapasitasHalamanBerikutnya)` returns ranges for multiple ordered sections.
- `RentangHalaman.getIndeksBagian()`, `getBarisMulai()`, `getBarisAkhir()`, and `isHalamanPertama()` expose zero-based, start-inclusive/end-exclusive ranges.

- [ ] **Step 1: Write the minimal implementation**

Implement `PaginasiLaporan` as a final class with a private constructor. Validate that row counts are non-negative and both capacities are positive; throw `IllegalArgumentException` for invalid values. Return an empty list for zero total rows. For each non-empty section, emit at least one range and then continue from the previous `barisAkhir` until all rows are covered. Mark only the first emitted range across all sections as `halamanPertama`.

The core loop should follow this shape:

```java
boolean halamanPertama = true;
for (int indeksBagian = 0; indeksBagian < jumlahBarisBagian.size(); indeksBagian++) {
    int jumlahBaris = jumlahBarisBagian.get(indeksBagian);
    int barisMulai = 0;
    while (barisMulai < jumlahBaris) {
        int kapasitas = halamanPertama
                ? kapasitasHalamanPertama : kapasitasHalamanBerikutnya;
        int barisAkhir = Math.min(jumlahBaris, barisMulai + kapasitas);
        halaman.add(new RentangHalaman(
                indeksBagian, barisMulai, barisAkhir, halamanPertama));
        halamanPertama = false;
        barisMulai = barisAkhir;
    }
}
```

`buat(...)` delegates to `buatBagian(Collections.singletonList(jumlahBaris), ...)` so both APIs share exactly one pagination rule.

- [ ] **Step 2: Run the focused test to verify it passes**

Run:

```bash
ant test
```

Expected result: the new pagination tests and all existing academic tests pass. If a range assertion fails, fix the utility implementation rather than weakening the test.

- [ ] **Step 3: Commit the green utility**

```bash
git add src/com/mahadi/indivaragroup/util/PaginasiLaporan.java
git commit -m "feat: add page-aware report pagination"
```

### Task 3: Add a failing regression test for the header rule

**Files:**
- Create: `test/com/mahadi/indivaragroup/ui/LaporanPanelTest.java`
- Modify: `build.xml:6-13` (`academic.test.classes`)

**Interfaces:**
- The test requires a package-private static `LaporanPanel.tampilkanKop(int pageIndex)` method that returns whether the report header is rendered for a page.

- [ ] **Step 1: Write the failing test**

Create a test that does not instantiate `LaporanPanel` or touch the database:

```java
@Test
public void rendersReportHeaderOnlyForFirstPage() {
    assertTrue(LaporanPanel.tampilkanKop(0));
    assertFalse(LaporanPanel.tampilkanKop(1));
    assertFalse(LaporanPanel.tampilkanKop(2));
}
```

Add `com.mahadi.indivaragroup.ui.LaporanPanelTest` to `academic.test.classes`.

- [ ] **Step 2: Run the test to verify it fails**

Run:

```bash
ant test
```

Expected result: test compilation fails because `LaporanPanel.tampilkanKop(int)` does not exist yet.

- [ ] **Step 3: Commit the red regression test**

```bash
git add build.xml test/com/mahadi/indivaragroup/ui/LaporanPanelTest.java
git commit -m "test: require first-page-only report header"
```

### Task 4: Integrate page-aware rendering into `LaporanPanel`

**Files:**
- Modify: `src/com/mahadi/indivaragroup/ui/LaporanPanel.java:434-515`
- Test: `test/com/mahadi/indivaragroup/ui/LaporanPanelTest.java`
- Test: `test/com/mahadi/indivaragroup/util/PaginasiLaporanTest.java`

**Interfaces:**
- `tampilkanKop(int pageIndex)` is package-private static and returns `pageIndex == 0`.
- The existing private `CetakLaporanPrintable` continues to implement `Printable`; no public UI API changes.

- [ ] **Step 1: Add the minimal header-rule helper and make the regression test pass**

Add near the report constants:

```java
static boolean tampilkanKop(int pageIndex) {
    return pageIndex == 0;
}
```

Run `ant test` and confirm `LaporanPanelTest` passes before changing pagination/rendering logic.

- [ ] **Step 2: Refactor ordinary report pagination to use the ranges**

Import `PaginasiLaporan` and add small private helpers inside `CetakLaporanPrintable`:

- one helper returns the fixed header height used by `gambarKopLaporan`, including the optional ranking convention line;
- one helper converts content height, reserved header height, and table-header height into `Math.max(1, ...)` rows per page.

In the ordinary-report branch of `print(...)`:

1. Calculate `tinggiKop` using the same lines already drawn by `gambarKopLaporan`.
2. Calculate `kapasitasHalamanPertama` with `tinggiKop` and `kapasitasHalamanBerikutnya` with zero header height, preserving the existing 150-pixel bottom reservation.
3. Build `List<PaginasiLaporan.RentangHalaman>` with `PaginasiLaporan.buat(tableModel.getRowCount(), ...)` before drawing anything.
4. Return `NO_SUCH_PAGE` if `pageIndex` is outside the range list.
5. Use the selected range's `barisMulai`/`barisAkhir` for `gambarTabel(...)`.
6. Call `gambarKopLaporan(...)` only when `tampilkanKop(pageIndex)` is true; otherwise set the table y-coordinate to `yAwal`.
7. Detect the final page from `pageIndex == halaman.size() - 1` and preserve the existing signature placement.

Do not draw the header before checking whether a range exists; an out-of-range `pageIndex` must return `NO_SUCH_PAGE` without rendering.

- [ ] **Step 3: Refactor TOPSIS report pagination to use section ranges**

In `cetakLaporanPerhitungan(...)`:

1. Collect each `Bagian.getBaris().size()` into an ordered `List<Integer>`.
2. Calculate first/continuation capacities with the existing TOPSIS reservation formula (`180` plus `TINGGI_HEADER_TABEL`), using the kop height only for the first capacity.
3. Call `PaginasiLaporan.buatBagian(...)` before drawing.
4. Return `NO_SUCH_PAGE` when `pageIndex` is outside the generated ranges.
5. Resolve the active `Bagian` through `range.getIndeksBagian()` and render only `range.getBarisMulai()` through `range.getBarisAkhir()`.
6. Call `gambarKopLaporan(...)` only when `tampilkanKop(pageIndex)` is true; otherwise use `yAwal` as the section baseline.
7. Keep the section title, table header, and existing final-section signature behavior. Use the generated final page index rather than the old `sisaHalaman` calculation.

The existing `gambarKopLaporan(...)` content, logo loading, fonts, table content, and signature text remain unchanged.

- [ ] **Step 4: Run focused and full tests**

Run:

```bash
ant test
```

Expected result: all academic tests pass, including the pagination and first-page-only header regression tests. Inspect the output for compilation errors or warnings before continuing.

- [ ] **Step 5: Commit the integration**

```bash
git add src/com/mahadi/indivaragroup/ui/LaporanPanel.java
git commit -m "fix: render report header only on first page"
```

### Task 5: Build/package verification and final diff review

**Files:**
- No additional source changes expected.

- [ ] **Step 1: Run the complete test suite again**

```bash
ant test
```

Expected: exit code `0` and `Semua test berhasil.`

- [ ] **Step 2: Build the application package**

```bash
ant clean jar
```

Expected: Java compilation and JAR packaging complete successfully.

- [ ] **Step 3: Review the final diff and repository state**

```bash
git diff HEAD~4..HEAD --stat
git diff HEAD~4..HEAD --check
git status --short
```

Confirm that only the pagination utility, its tests, `LaporanPanel`, `build.xml`, and the design/plan documents changed. Do not modify or remove unrelated user files.

- [ ] **Step 4: Commit any necessary test-wiring correction**

If the final review identifies only a missing test-list entry or formatting correction, fix that narrow issue and commit it with:

```bash
git add build.xml test src
git commit -m "test: finalize report pagination verification"
```

Do not create this commit if no correction is needed.
