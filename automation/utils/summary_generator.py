import os
import time
from collections import defaultdict
from automation.config.config import Config

class SummaryGenerator:
    """Generates Markdown summaries for local viewing and GitHub Step Summaries."""

    def __init__(self, results, duration_sec=0, build_status="PASS", deployment_status="PASS"):
        self.results = results
        self.duration_sec = duration_sec
        self.build_status = build_status
        self.deployment_status = deployment_status
        Config.initialize_directories()

    def generate_summary(self):
        total = len(self.results)
        passed = sum(1 for r in self.results if r["status"] == "PASS")
        failed = sum(1 for r in self.results if r["status"] == "FAIL")
        skipped = total - (passed + failed)
        pass_pct = (passed / total * 100) if total > 0 else 0.0
        timestamp = time.strftime("%Y-%m-%d %H:%M:%S UTC", time.gmtime())

        # Module statistics
        mod_stats = defaultdict(lambda: {"total": 0, "passed": 0, "failed": 0})
        for r in self.results:
            m = r["module"]
            mod_stats[m]["total"] += 1
            if r["status"] == "PASS":
                mod_stats[m]["passed"] += 1
            elif r["status"] == "FAIL":
                mod_stats[m]["failed"] += 1

        top_passing = []
        top_failing = []
        for m, s in mod_stats.items():
            rate = (s["passed"] / s["total"] * 100) if s["total"] > 0 else 0
            if s["failed"] > 0:
                top_failing.append((m, s["failed"], s["total"]))
            top_passing.append((m, rate))

        top_passing.sort(key=lambda x: x[1], reverse=True)
        top_failing.sort(key=lambda x: x[1], reverse=True)

        markdown = f"""# Live GitHub Pages E2E Execution Summary

**Deployment URL:**
{Config.BASE_URL}

**Execution Date:**
{timestamp}

**Build Status:**
{self.build_status}

**Deployment Status:**
{self.deployment_status}

**Total Test Cases:**
{total}

**Executed:** {total}
**Passed:** {passed}
**Failed:** {failed}
**Skipped:** {skipped}

**Pass Percentage:**
{pass_pct:.2f}%

**Execution Duration:**
{self.duration_sec:.2f} seconds

---

### Top Failed Modules:
"""
        if top_failing:
            for m, f_cnt, t_cnt in top_failing:
                markdown += f"- **{m}**: {f_cnt}/{t_cnt} failed\n"
        else:
            markdown += "- None (Zero module failures detected)\n"

        markdown += "\n### Failed Tests:\n"
        failed_tests = [r for r in self.results if r["status"] == "FAIL"]
        if failed_tests:
            for r in failed_tests[:10]:
                markdown += f"- **{r['test_id']}** - {r['name']} | *Reason:* {r.get('failure_reason', 'N/A')}\n"
        else:
            markdown += "- None (100% of executed test cases passed)\n"

        markdown += "\n### Top Passing Modules:\n"
        for m, rate in top_passing[:5]:
            markdown += f"- **{m}**: {rate:.1f}% Pass Rate\n"

        markdown += """
---

### Artifacts Generated:
- ✓ Excel Reports (`Automation_Test_Report.xlsx`, `Failed_Test_Cases.xlsx`, `Passed_Test_Cases.xlsx`, `Summary_Report.xlsx`)
- ✓ HTML Reports (`execution-report.html`, `dashboard.html`)
- ✓ Screenshots (`Screenshots/`)
- ✓ Logs (`Logs/`)
- ✓ JSON Results (`execution-results.json`)
"""

        # Write summary.md
        filepath = os.path.join(Config.SUMMARY_DIR, "summary.md")
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(markdown)

        # Append to GITHUB_STEP_SUMMARY if present
        github_step_summary = os.environ.get("GITHUB_STEP_SUMMARY")
        if github_step_summary:
            try:
                with open(github_step_summary, "a", encoding="utf-8") as f:
                    f.write(markdown)
            except Exception as e:
                print(f"Notice: Could not write to GITHUB_STEP_SUMMARY: {e}")

        return markdown
