// =============================================================
// 📦 Chatwork Sender Frontend（完全安定版 + グループ編集対応）
// =============================================================

let allRooms = [];
let groupSets = [];

// -------------------------------------------------------------
// 🚀 初期化処理
// -------------------------------------------------------------
document.addEventListener("DOMContentLoaded", async () => {
  await loadToken();
  await loadRooms();
  await loadGroupSets();
});

// -------------------------------------------------------------
// 🔑 Chatwork トークン関連
// -------------------------------------------------------------
document.getElementById("saveTokenBtn").addEventListener("click", async () => {
  const token = document.getElementById("token").value.trim();
  if (!token) return alert("トークンを入力してください");

  const res = await fetch("/api/user/token", {
    method: "POST",
    headers: { "Content-Type": "text/plain" },
    body: token,
  });

  if (res.ok) {
    alert("✅ トークンを保存しました");
    await loadRooms();
  } else {
    alert("❌ トークン保存に失敗しました");
  }
});

async function loadToken() {
  try {
    const res = await fetch("/api/user/token");
    if (!res.ok) throw new Error("トークン取得失敗");
    const data = await res.json();
    if (data.token) {
      document.getElementById("token").value = data.token;
      console.log("🔑 Chatworkトークン読み込み成功");
    }
  } catch (e) {
    console.error("トークン取得エラー:", e);
  }
}

// -------------------------------------------------------------
// 💬 ルーム取得・描画
// -------------------------------------------------------------
async function loadRooms() {
  const roomsContainer = document.getElementById("rooms");
  roomsContainer.innerHTML = "読み込み中...";

  try {
    const res = await fetch("/api/chatwork/rooms");
    if (!res.ok) throw new Error("ルーム取得に失敗しました");

    const rooms = await res.json();
    if (!rooms.length) {
      roomsContainer.innerHTML = `<p style="color:red;">ルームが取得できません。トークンを確認してください。</p>`;
      return;
    }
    allRooms = rooms;
    renderRooms();
  } catch (e) {
    roomsContainer.innerHTML = `<p style="color:red;">${e.message}</p>`;
  }
}

function renderRooms(selectedIds = []) {
  const roomsContainer = document.getElementById("rooms");
  roomsContainer.innerHTML = allRooms
    .map(
      (r) => `
      <label>
        <input type="checkbox" value="${r.room_id}" ${
        selectedIds.includes(r.room_id) ? "checked" : ""
      }>
        ${r.name}
      </label>`
    )
    .join("");
}

// -------------------------------------------------------------
// 📤 一斉送信
// -------------------------------------------------------------
document.getElementById("sendBtn")?.addEventListener("click", async () => {
  const message = document.getElementById("message").value.trim();
  const fileInput = document.getElementById("fileInput");
  const roomIds = Array.from(document.querySelectorAll("#rooms input:checked")).map((cb) =>
    parseInt(cb.value)
  );

  if (!roomIds.length) return alert("送信先を選択してください");

  const formData = new FormData();
  formData.append("message", message);
  formData.append("roomIds", JSON.stringify(roomIds));
  if (fileInput.files.length > 0) formData.append("file", fileInput.files[0]);

  const statusEl =
    document.getElementById("statusArea") || document.createElement("p");
  statusEl.id = "statusArea";
  statusEl.style.marginTop = "10px";
  statusEl.style.fontWeight = "bold";
  document.querySelector("section:nth-of-type(3)").appendChild(statusEl);
  statusEl.textContent = "🚀 送信開始...";

  try {
    const res = await fetch("/api/chatwork/send", { method: "POST", body: formData });
    if (!res.ok) throw new Error("送信に失敗しました");

    // 状態監視
    const intervalId = setInterval(async () => {
      try {
        const r = await fetch("/api/chatwork/status");
        const status = await r.text();
        statusEl.textContent = "📡 状態: " + status;
        if (/完了|エラー|中止/.test(status)) clearInterval(intervalId);
      } catch {
        clearInterval(intervalId);
      }
    }, 2000);
  } catch (e) {
    statusEl.textContent = "❌ エラーが発生しました";
  }

  fileInput.value = "";
  document.getElementById("message").value = "";
});

// -------------------------------------------------------------
// 🛑 中止
// -------------------------------------------------------------
document.getElementById("cancelBtn")?.addEventListener("click", async () => {
  await fetch("/api/chatwork/cancel", { method: "POST" });
  const statusEl =
    document.getElementById("statusArea") || document.createElement("p");
  statusEl.id = "statusArea";
  document.querySelector("section:nth-of-type(3)").appendChild(statusEl);
  statusEl.textContent = "⏹️ 中止要求を送信しました...";
});

// -------------------------------------------------------------
// 🧩 グループセット機能
// -------------------------------------------------------------
async function loadGroupSets() {
  const res = await fetch("/api/groupset");
  if (!res.ok) return alert("❌ グループセットの取得に失敗しました");
  groupSets = await res.json();

  const select = document.getElementById("groupSetSelect");
  let options = `<option value="">（未選択）</option>`;
  options += groupSets
    .map((g) => `<option value="${g.name}">${g.name}</option>`)
    .join("");
  select.innerHTML = options;
}

// ✅ セット選択でチェック反映
document.getElementById("groupSetSelect").addEventListener("change", (e) => {
  const selected = groupSets.find((g) => g.name === e.target.value);
  renderRooms(selected ? selected.roomIds : []);
});

// ✅ 新規保存 / 上書き保存
document.getElementById("newGroupBtn").addEventListener("click", async () => {
  const name = prompt("グループセット名を入力してください：");
  if (!name) return;

  const roomIds = getCheckedRoomIds();
  if (!roomIds.length) return alert("少なくとも1つ選択してください。");

  const existing = groupSets.find((g) => g.name === name);
  if (existing && !confirm(`「${name}」は既に存在します。上書きしますか？`)) return;

  const res = await fetch("/api/groupset", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, roomIds }),
  });

  if (res.ok) {
    const msg = existing ? `🔄 「${name}」を上書きしました。` : `✅ 「${name}」を保存しました。`;
    alert(msg);
    await loadGroupSets();
    document.getElementById("groupSetSelect").value = name;
  } else {
    alert("❌ 保存に失敗しました。");
  }
});

// ✅ 上書き保存
document.getElementById("updateGroupBtn").addEventListener("click", async () => {
  const select = document.getElementById("groupSetSelect");
  const name = select.value.trim();
  if (!name) return alert("上書きするグループセットを選択してください。");

  // 選択されているルームIDを取得
  const roomIds = Array.from(document.querySelectorAll("#rooms input:checked")).map((cb) =>
    parseInt(cb.value)
  );

  if (roomIds.length === 0) {
    return alert("少なくとも1つのルームを選択してください。");
  }

  // 上書きリクエスト送信
  const res = await fetch("/api/groupset", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, roomIds }),
  });

  if (res.ok) {
    const data = await res.json().catch(() => ({}));
    alert(data.message || `🔄 「${name}」を上書きしました。`);
    await loadGroupSets();
    select.value = name; // 再選択を維持
  } else {
    alert("❌ 上書き保存に失敗しました。");
  }
});

// ✅ 削除
document.getElementById("deleteGroupBtn").addEventListener("click", async () => {
  const name = document.getElementById("groupSetSelect").value;
  if (!name) return alert("削除するセットを選択してください");
  if (!confirm(`「${name}」を削除しますか？`)) return;

  const res = await fetch(`/api/groupset/${encodeURIComponent(name)}`, { method: "DELETE" });
  if (res.ok) {
    alert("🗑️ 削除しました");
    await loadGroupSets();
    renderRooms([]);
  } else {
    alert("❌ 削除に失敗しました。");
  }
});

// ✅ 名前変更
document.getElementById("renameGroupBtn")?.addEventListener("click", async () => {
  const oldName = document.getElementById("groupSetSelect").value;
  if (!oldName) return alert("変更するセットを選択してください");
  const newName = prompt("新しいグループセット名を入力：", oldName);
  if (!newName || newName === oldName) return;

  const res = await fetch("/api/groupset/rename", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ oldName, newName }),
  });

  if (res.ok) {
    alert("✏️ 名前を変更しました");
    await loadGroupSets();
    document.getElementById("groupSetSelect").value = newName;
  } else {
    alert("❌ 名前変更に失敗しました");
  }
});

// ✅ 選択済みルーム取得
function getCheckedRoomIds() {
  return Array.from(document.querySelectorAll("#rooms input:checked")).map((cb) =>
    parseInt(cb.value)
  );
}

// -------------------------------------------------------------
// 🕓 履歴読み込み
// -------------------------------------------------------------
document.getElementById("loadHistoryBtn").addEventListener("click", async () => {
  const res = await fetch("/api/history");
  const div = document.getElementById("historyArea");

  if (!res.ok) {
    div.innerHTML = "<p style='color:red;'>履歴取得に失敗しました。</p>";
    return;
  }

  const logs = await res.json();
  if (!logs.length) {
    div.innerHTML = "<p>履歴がありません。</p>";
    return;
  }

  div.innerHTML = logs
    .map(
      (log) => `
      <div style="border-bottom:1px solid #ddd;margin-bottom:6px;padding-bottom:4px;">
        <b>${log.roomName}</b>（ID: ${log.roomId}）<br>
        <small>${log.sentAt?.replace("T", " ").split(".")[0] || ""}</small><br>
        <span>${log.message || "（ファイル送信）"}</span><br>
        ${log.fileName ? `<small>📎 ${log.fileName}</small>` : ""}
      </div>`
    )
    .join("");
});
