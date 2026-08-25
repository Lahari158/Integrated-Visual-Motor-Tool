import os
import json
import time
from automation.config.config import Config

class HTMLReporter:
    """Generates standalone interactive HTML reports for Selenium E2E execution results."""

    def __init__(self, results, duration_sec=0):
        self.results = results
        self.duration_sec = duration_sec
        Config.initialize_directories()

    def generate_all_html_reports(self):
        self._generate_execution_report()
        self._generate_dashboard()
        self._generate_json_results()

    def _generate_execution_report(self):
        total = len(self.results)
        passed = sum(1 for r in self.results if r["status"] == "PASS")
        failed = sum(1 for r in self.results if r["status"] == "FAIL")
        skipped = total - (passed + failed)
        pass_pct = (passed / total * 100) if total > 0 else 0.0

        html_content = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Selenium E2E Test Execution Report</title>
    <style>
        body {{ font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #F4F6F9; color: #2D3748; padding: 2rem; margin: 0; }}
        .header {{ background: #1F4E78; color: white; padding: 1.5rem; border-radius: 8px; margin-bottom: 2rem; display: flex; justify-content: space-between; align-items: center; }}
        .kpi-container {{ display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; margin-bottom: 2rem; }}
        .kpi-card {{ background: white; padding: 1.25rem; border-radius: 8px; border: 1px solid #E2E8F0; text-align: center; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }}
        .kpi-val {{ font-size: 2rem; font-weight: bold; margin-top: 5px; }}
        .pass-val {{ color: #276A3C; }}
        .fail-val {{ color: #9C0006; }}
        .table-card {{ background: white; padding: 1.5rem; border-radius: 8px; border: 1px solid #E2E8F0; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }}
        table {{ width: 100%; border-collapse: collapse; margin-top: 1rem; }}
        th, td {{ padding: 10px 14px; border-bottom: 1px solid #E2E8F0; text-align: left; font-size: 0.9rem; }}
        th {{ background: #1F4E78; color: white; }}
        .badge {{ padding: 4px 10px; border-radius: 12px; font-weight: bold; font-size: 0.75rem; text-transform: uppercase; }}
        .badge-pass {{ background: #E2EFDA; color: #276A3C; }}
        .badge-fail {{ background: #FCE4D6; color: #9C0006; }}
        .badge-skip {{ background: #FFF2CC; color: #B7791F; }}
        .filter-box {{ display: flex; gap: 1rem; margin-bottom: 1rem; }}
        input, select {{ padding: 8px 12px; border-radius: 4px; border: 1px solid #E2E8F0; }}
    </style>
</head>
<body>
    <div class="header">
        <div>
            <h1 style="margin: 0; font-size: 1.5rem;">Visual Monitor Trainer — Live Selenium E2E Test Report</h1>
            <p style="margin: 5px 0 0 0; opacity: 0.85;">Target URL: {Config.BASE_URL}</p>
        </div>
        <div>
            <span>Execution Duration: <strong>{self.duration_sec:.2f}s</strong></span>
        </div>
    </div>

    <div class="kpi-container">
        <div class="kpi-card"><div>Total Tests</div><div class="kpi-val">{total}</div></div>
        <div class="kpi-card"><div>Passed</div><div class="kpi-val pass-val">{passed}</div></div>
        <div class="kpi-card"><div>Failed</div><div class="kpi-val fail-val">{failed}</div></div>
        <div class="kpi-card"><div>Skipped</div><div class="kpi-val">{skipped}</div></div>
        <div class="kpi-card"><div>Pass Percentage</div><div class="kpi-val pass-val">{pass_pct:.2f}%</div></div>
    </div>

    <div class="table-card">
        <h3>Detailed Executed Test Cases</h3>
        <div class="filter-box">
            <input type="text" id="searchInput" placeholder="Search Test ID or Module..." onkeyup="filterReport()">
            <select id="statusFilter" onchange="filterReport()">
                <option value="ALL">All Statuses</option>
                <option value="PASS">PASS</option>
                <option value="FAIL">FAIL</option>
            </select>
        </div>

        <table id="reportTable">
            <thead>
                <tr>
                    <th>Test ID</th>
                    <th>Module</th>
                    <th>Test Name</th>
                    <th>Priority</th>
                    <th>Status</th>
                    <th>Time (ms)</th>
                </tr>
            </thead>
            <tbody>
"""
        for r in self.results:
            b_class = "badge-pass" if r["status"] == "PASS" else ("badge-fail" if r["status"] == "FAIL" else "badge-skip")
            html_content += f"""
                <tr>
                    <td><strong>{r['test_id']}</strong></td>
                    <td>{r['module']}</td>
                    <td>{r['name']}</td>
                    <td>{r['priority']}</td>
                    <td><span class="badge {b_class}">{r['status']}</span></td>
                    <td>{r['exec_time_ms']}</td>
                </tr>
"""
        html_content += """
            </tbody>
        </table>
    </div>

    <script>
        function filterReport() {
            const q = document.getElementById('searchInput').value.toLowerCase();
            const status = document.getElementById('statusFilter').value;
            const rows = document.querySelectorAll('#reportTable tbody tr');

            rows.forEach(row => {
                const text = row.innerText.toLowerCase();
                const matchQ = text.includes(q);
                const matchS = status === 'ALL' || text.includes(status.toLowerCase());
                row.style.display = (matchQ && matchS) ? '' : 'none';
            });
        }
    </script>
</body>
</html>
"""
        filepath = os.path.join(Config.HTML_DIR, "execution-report.html")
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(html_content)

    def _generate_dashboard(self):
        total = len(self.results)
        passed = sum(1 for r in self.results if r["status"] == "PASS")
        failed = sum(1 for r in self.results if r["status"] == "FAIL")
        pass_pct = (passed / total * 100) if total > 0 else 0.0

        html = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Selenium Test Analytics Dashboard</title>
    <style>
        body {{ font-family: sans-serif; background: #0F172A; color: #F8FAFC; padding: 2rem; }}
        .card {{ background: #1E293B; border-radius: 8px; padding: 1.5rem; margin-bottom: 1.5rem; border: 1px solid #334155; }}
        .grid {{ display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.5rem; }}
        .metric {{ font-size: 2.5rem; font-weight: bold; margin-top: 10px; color: #38BDF8; }}
    </style>
</head>
<body>
    <h1>🚀 Live Production Selenium Dashboard</h1>
    <div class="grid">
        <div class="card"><div>Total Executed</div><div class="metric">{total}</div></div>
        <div class="card"><div>Pass Percentage</div><div class="metric" style="color: #4ADE80;">{pass_pct:.1f}%</div></div>
        <div class="card"><div>Total Failures</div><div class="metric" style="color: #F87171;">{failed}</div></div>
    </div>
</body>
</html>"""
        filepath = os.path.join(Config.HTML_DIR, "dashboard.html")
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(html)

    def _generate_json_results(self):
        filepath = os.path.join(Config.JSON_DIR, "execution-results.json")
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump({
                "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
                "base_url": Config.BASE_URL,
                "total_test_cases": len(self.results),
                "passed": sum(1 for r in self.results if r["status"] == "PASS"),
                "failed": sum(1 for r in self.results if r["status"] == "FAIL"),
                "results": self.results
            }, f, indent=2)
