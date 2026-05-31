import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  BarChart3,
  Download,
  FileClock,
  LogOut,
  Plus,
  Search,
  Send,
  Settings,
  UserRound
} from "lucide-react";
import {
  api,
  Dashboard,
  Department,
  MasterUser,
  ReportHistory,
  User,
  WorkReportResult
} from "./api";

type View = "dashboard" | "register" | "search" | "reports" | "histories" | "masters";

const categories = [
  ["DESIGN", "設計"],
  ["DEVELOPMENT", "開発"],
  ["TEST", "テスト"],
  ["MEETING", "会議"],
  ["DOCUMENT", "ドキュメント"],
  ["OTHER", "その他"]
];

export default function App() {
  const [user, setUser] = useState<User | null>(null);
  const [view, setView] = useState<View>("dashboard");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.me()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <main className="boot">読み込み中</main>;
  }

  if (!user) {
    return <LoginScreen onLogin={setUser} error={error} setError={setError} />;
  }

  const nav = [
    { id: "dashboard" as const, label: "ダッシュボード", icon: BarChart3 },
    { id: "register" as const, label: "日報登録", icon: Plus },
    { id: "search" as const, label: "実績検索", icon: Search },
    { id: "reports" as const, label: "帳票出力", icon: Download },
    { id: "histories" as const, label: "履歴", icon: FileClock },
    ...(user.roleCode === "ADMIN"
      ? [{ id: "masters" as const, label: "マスタ管理", icon: Settings }]
      : [])
  ];

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">Work Report</div>
        <nav className="nav">
          {nav.map((item) => {
            const Icon = item.icon;
            return (
              <button
                key={item.id}
                className={view === item.id ? "active" : ""}
                onClick={() => setView(item.id)}
                title={item.label}
              >
                <Icon size={18} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>
      </aside>

      <main className="workspace">
        <header className="topbar">
          <div>
            <p>{user.departmentName}</p>
            <h1>{titleFor(view)}</h1>
          </div>
          <div className="account">
            <UserRound size={18} />
            <span>{user.employeeName}</span>
            <button
              title="ログアウト"
              className="iconButton"
              onClick={() => api.logout().then(() => setUser(null))}
            >
              <LogOut size={18} />
            </button>
          </div>
        </header>

        {error && <div className="alert">{error}</div>}
        {view === "dashboard" && <DashboardView setError={setError} />}
        {view === "register" && <RegisterView setError={setError} />}
        {view === "search" && <SearchView setError={setError} />}
        {view === "reports" && <ReportExportView user={user} setError={setError} />}
        {view === "histories" && <HistoriesView setError={setError} />}
        {view === "masters" && <MasterView setError={setError} />}
      </main>
    </div>
  );
}

function LoginScreen({
  onLogin,
  error,
  setError
}: {
  onLogin: (user: User) => void;
  error: string;
  setError: (error: string) => void;
}) {
  const [loginId, setLoginId] = useState("admin");
  const [password, setPassword] = useState("password");
  const [submitting, setSubmitting] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    try {
      onLogin(await api.login(loginId, password));
    } catch {
      setError("ログインIDまたはパスワードが正しくありません。");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="login">
      <form className="loginPanel" onSubmit={submit}>
        <h1>Work Report</h1>
        {error && <div className="alert">{error}</div>}
        <label>
          ログインID
          <input value={loginId} onChange={(event) => setLoginId(event.target.value)} />
        </label>
        <label>
          パスワード
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
        </label>
        <button className="primary" disabled={submitting}>
          <Send size={18} />
          <span>ログイン</span>
        </button>
      </form>
    </main>
  );
}

function DashboardView({ setError }: { setError: (error: string) => void }) {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);

  useEffect(() => {
    api.dashboard()
      .then(setDashboard)
      .catch((error: Error) => setError(error.message));
  }, [setError]);

  if (!dashboard) {
    return <section className="panel">読み込み中</section>;
  }

  return (
    <section className="stack">
      <div className="metrics">
        <Metric label="本日登録" value={`${dashboard.todayWorkReportCount}件`} />
        <Metric label="当月時間" value={`${dashboard.currentMonthTotalHours}h`} tone="green" />
        <Metric label="未出力帳票" value={`${dashboard.notOutputMonthlyReportCount}件`} tone="amber" />
      </div>
      <div className="panel">
        <h2>最近の活動</h2>
        <div className="activityList">
          {dashboard.recentActivities.map((activity, index) => (
            <div className="activity" key={`${activity.activityAt}-${index}`}>
              <span className={`badge ${activity.badgeClass}`}>{activity.activityTypeName}</span>
              <strong>{activity.employeeName}</strong>
              <span>{activity.content}</span>
              <time>{activity.activityAt}</time>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function RegisterView({ setError }: { setError: (error: string) => void }) {
  const today = new Date().toISOString().slice(0, 10);
  const [saved, setSaved] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setError("");
    setSaved("");
    try {
      const result = await api.registerWorkReport({
        workDate: form.get("workDate"),
        projectName: form.get("projectName"),
        workCategory: form.get("workCategory"),
        workHours: Number(form.get("workHours")),
        workContent: form.get("workContent")
      });
      setSaved(`登録しました: #${result.workReportId}`);
      event.currentTarget.reset();
    } catch (error) {
      setError(messageOf(error));
    }
  }

  return (
    <form className="panel formGrid" onSubmit={submit}>
      {saved && <div className="success">{saved}</div>}
      <label>
        作業日
        <input name="workDate" type="date" defaultValue={today} required />
      </label>
      <label>
        プロジェクト名
        <input name="projectName" maxLength={100} required />
      </label>
      <label>
        作業分類
        <select name="workCategory" required>
          {categories.map(([value, label]) => (
            <option value={value} key={value}>
              {label}
            </option>
          ))}
        </select>
      </label>
      <label>
        作業時間
        <input name="workHours" type="number" step="0.25" min="0.25" max="24" required />
      </label>
      <label className="wide">
        作業内容
        <textarea name="workContent" maxLength={1000} required />
      </label>
      <button className="primary">
        <Send size={18} />
        <span>登録</span>
      </button>
    </form>
  );
}

function SearchView({ setError }: { setError: (error: string) => void }) {
  const [results, setResults] = useState<WorkReportResult[]>([]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const query = compactForm(new FormData(event.currentTarget));
    setError("");
    try {
      setResults(await api.searchWorkReports(query));
    } catch (error) {
      setError(messageOf(error));
    }
  }

  return (
    <section className="stack">
      <form className="panel filters" onSubmit={submit}>
        <input name="dateFrom" type="date" aria-label="対象期間 From" />
        <input name="dateTo" type="date" aria-label="対象期間 To" />
        <input name="employeeName" placeholder="社員名" />
        <input name="departmentName" placeholder="部署名" />
        <select name="workCategory" defaultValue="">
          <option value="">分類</option>
          {categories.map(([value, label]) => (
            <option value={value} key={value}>
              {label}
            </option>
          ))}
        </select>
        <input name="projectName" placeholder="プロジェクト" />
        <button className="primary" title="検索">
          <Search size={18} />
          <span>検索</span>
        </button>
      </form>
      <ResultTable rows={results} />
    </section>
  );
}

function ReportExportView({
  user,
  setError
}: {
  user: User;
  setError: (error: string) => void;
}) {
  const [users, setUsers] = useState<User[]>([]);
  const targetUsers = useMemo(() => (user.roleCode === "ADMIN" ? users : [user]), [user, users]);
  const defaultMonth = new Date().toISOString().slice(0, 7);

  useEffect(() => {
    if (user.roleCode === "ADMIN") {
      api.targetUsers().then(setUsers).catch((error: Error) => setError(error.message));
    }
  }, [setError, user.roleCode]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setError("");
    try {
      await api.exportMonthlyReport({
        targetYearMonth: String(form.get("targetYearMonth")).replace("-", ""),
        targetUserId: Number(form.get("targetUserId"))
      });
    } catch (error) {
      setError(messageOf(error));
    }
  }

  return (
    <form className="panel formGrid" onSubmit={submit}>
      <label>
        対象年月
        <input name="targetYearMonth" type="month" defaultValue={defaultMonth} required />
      </label>
      <label>
        対象者
        <select name="targetUserId" defaultValue={targetUsers[0]?.userId} required>
          {targetUsers.map((target) => (
            <option value={target.userId} key={target.userId}>
              {target.employeeName}
            </option>
          ))}
        </select>
      </label>
      <button className="primary">
        <Download size={18} />
        <span>出力</span>
      </button>
    </form>
  );
}

function HistoriesView({ setError }: { setError: (error: string) => void }) {
  const [histories, setHistories] = useState<ReportHistory[]>([]);

  async function load(query: Record<string, string> = {}) {
    setError("");
    try {
      setHistories(await api.histories(query));
    } catch (error) {
      setError(messageOf(error));
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await load(compactForm(new FormData(event.currentTarget)));
  }

  return (
    <section className="stack">
      <form className="panel filters" onSubmit={submit}>
        <input name="targetYearMonth" placeholder="YYYYMM" inputMode="numeric" maxLength={6} />
        <select name="status" defaultValue="">
          <option value="">状態</option>
          <option value="SUCCESS">成功</option>
          <option value="PROCESSING">処理中</option>
          <option value="ERROR">エラー</option>
        </select>
        <button className="primary">
          <Search size={18} />
          <span>検索</span>
        </button>
      </form>
      <div className="panel tableWrap">
        <table>
          <thead>
            <tr>
              <th>対象年月</th>
              <th>対象者</th>
              <th>作成者</th>
              <th>ファイル</th>
              <th>状態</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {histories.map((history) => (
              <tr key={history.reportOutputHistoryId}>
                <td>{history.targetYearMonth}</td>
                <td>{history.targetEmployeeName}</td>
                <td>{history.createdByEmployeeName}</td>
                <td>{history.fileName}</td>
                <td>
                  <span className={`badge ${history.status.toLowerCase()}`}>{history.status}</span>
                </td>
                <td>
                  {history.status === "SUCCESS" && (
                    <button
                      className="iconButton"
                      title="ダウンロード"
                      onClick={() => api.downloadHistory(history.reportOutputHistoryId)}
                    >
                      <Download size={17} />
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function MasterView({ setError }: { setError: (error: string) => void }) {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [users, setUsers] = useState<MasterUser[]>([]);
  const [departmentEditing, setDepartmentEditing] = useState<Department | null>(null);
  const [userEditing, setUserEditing] = useState<MasterUser | null>(null);
  const [message, setMessage] = useState("");

  async function load() {
    setError("");
    try {
      const [nextDepartments, nextUsers] = await Promise.all([
        api.departments(),
        api.masterUsers()
      ]);
      setDepartments(nextDepartments);
      setUsers(nextUsers);
    } catch (error) {
      setError(messageOf(error));
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function submitDepartment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    const payload = {
      departmentCode: String(form.get("departmentCode") ?? ""),
      departmentName: String(form.get("departmentName") ?? ""),
      displayOrder: Number(form.get("displayOrder") || 0)
    };
    setError("");
    setMessage("");
    try {
      if (departmentEditing) {
        await api.updateDepartment(departmentEditing.departmentId, payload);
        setMessage("部署を更新しました。");
      } else {
        await api.createDepartment(payload);
        setMessage("部署を追加しました。");
      }
      setDepartmentEditing(null);
      formElement.reset();
      await load();
    } catch (error) {
      setError(messageOf(error));
    }
  }

  async function submitUser(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    const payload = {
      departmentId: Number(form.get("departmentId")),
      loginId: String(form.get("loginId") ?? ""),
      password: String(form.get("password") ?? ""),
      employeeName: String(form.get("employeeName") ?? ""),
      roleCode: String(form.get("roleCode") ?? "")
    };
    setError("");
    setMessage("");
    try {
      if (userEditing) {
        await api.updateMasterUser(userEditing.userId, payload);
        setMessage("ユーザーを更新しました。");
      } else {
        await api.createMasterUser(payload);
        setMessage("ユーザーを追加しました。");
      }
      setUserEditing(null);
      formElement.reset();
      await load();
    } catch (error) {
      setError(messageOf(error));
    }
  }

  return (
    <section className="stack">
      {message && <div className="success standalone">{message}</div>}
      <div className="masterGrid">
        <form className="panel formGrid" onSubmit={submitDepartment} key={departmentEditing?.departmentId ?? "department-new"}>
          <h2 className="wide">{departmentEditing ? "部署編集" : "部署追加"}</h2>
          <label>
            部署コード
            <input
              name="departmentCode"
              defaultValue={departmentEditing?.departmentCode ?? ""}
              maxLength={20}
              required
            />
          </label>
          <label>
            部署名
            <input
              name="departmentName"
              defaultValue={departmentEditing?.departmentName ?? ""}
              maxLength={100}
              required
            />
          </label>
          <label>
            表示順
            <input
              name="displayOrder"
              type="number"
              min="0"
              defaultValue={departmentEditing?.displayOrder ?? 0}
              required
            />
          </label>
          <div className="buttonRow">
            <button className="primary">
              <Send size={18} />
              <span>{departmentEditing ? "更新" : "追加"}</span>
            </button>
            {departmentEditing && (
              <button type="button" className="secondary" onClick={() => setDepartmentEditing(null)}>
                取消
              </button>
            )}
          </div>
        </form>

        <form className="panel formGrid" onSubmit={submitUser} key={userEditing?.userId ?? "user-new"}>
          <h2 className="wide">{userEditing ? "ユーザー編集" : "ユーザー追加"}</h2>
          <label>
            部署
            <select name="departmentId" defaultValue={userEditing?.departmentId ?? departments[0]?.departmentId} required>
              {departments.map((department) => (
                <option value={department.departmentId} key={department.departmentId}>
                  {department.departmentName}
                </option>
              ))}
            </select>
          </label>
          <label>
            ログインID
            <input name="loginId" defaultValue={userEditing?.loginId ?? ""} maxLength={50} required />
          </label>
          <label>
            社員名
            <input name="employeeName" defaultValue={userEditing?.employeeName ?? ""} maxLength={100} required />
          </label>
          <label>
            権限
            <select name="roleCode" defaultValue={userEditing?.roleCode ?? "USER"}>
              <option value="USER">一般ユーザー</option>
              <option value="ADMIN">管理者</option>
            </select>
          </label>
          <label className="wide">
            パスワード
            <input
              name="password"
              type="password"
              minLength={8}
              placeholder={userEditing ? "変更する場合のみ入力" : ""}
              required={!userEditing}
            />
          </label>
          <div className="buttonRow">
            <button className="primary">
              <Send size={18} />
              <span>{userEditing ? "更新" : "追加"}</span>
            </button>
            {userEditing && (
              <button type="button" className="secondary" onClick={() => setUserEditing(null)}>
                取消
              </button>
            )}
          </div>
        </form>
      </div>

      <div className="panel tableWrap">
        <h2>部署一覧</h2>
        <table>
          <thead>
            <tr>
              <th>コード</th>
              <th>部署名</th>
              <th>表示順</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {departments.map((department) => (
              <tr key={department.departmentId}>
                <td>{department.departmentCode}</td>
                <td>{department.departmentName}</td>
                <td>{department.displayOrder}</td>
                <td>
                  <button className="secondary compact" onClick={() => setDepartmentEditing(department)}>
                    編集
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="panel tableWrap">
        <h2>ユーザー一覧</h2>
        <table>
          <thead>
            <tr>
              <th>ログインID</th>
              <th>社員名</th>
              <th>部署</th>
              <th>権限</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {users.map((masterUser) => (
              <tr key={masterUser.userId}>
                <td>{masterUser.loginId}</td>
                <td>{masterUser.employeeName}</td>
                <td>{masterUser.departmentName}</td>
                <td>{masterUser.roleCode}</td>
                <td>
                  <button className="secondary compact" onClick={() => setUserEditing(masterUser)}>
                    編集
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function Metric({ label, value, tone = "blue" }: { label: string; value: string; tone?: string }) {
  return (
    <div className={`metric ${tone}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function ResultTable({ rows }: { rows: WorkReportResult[] }) {
  return (
    <div className="panel tableWrap">
      <table>
        <thead>
          <tr>
            <th>作業日</th>
            <th>社員</th>
            <th>部署</th>
            <th>プロジェクト</th>
            <th>分類</th>
            <th>時間</th>
            <th>内容</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row, index) => (
            <tr key={`${row.workDate}-${row.employeeName}-${index}`}>
              <td>{row.workDate}</td>
              <td>{row.employeeName}</td>
              <td>{row.departmentName}</td>
              <td>{row.projectName}</td>
              <td>{row.workCategoryName}</td>
              <td>{row.workHours}</td>
              <td>{row.workContent}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function compactForm(form: FormData) {
  const query: Record<string, string> = {};
  form.forEach((value, key) => {
    if (typeof value === "string" && value.trim()) {
      query[key] = value.trim();
    }
  });
  return query;
}

function titleFor(view: View) {
  switch (view) {
    case "register":
      return "作業日報登録";
    case "search":
      return "作業実績検索";
    case "reports":
      return "月次報告書出力";
    case "histories":
      return "帳票作成履歴";
    case "masters":
      return "マスタ管理";
    default:
      return "ダッシュボード";
  }
}

function messageOf(error: unknown) {
  return error instanceof Error ? error.message : "処理に失敗しました。";
}
