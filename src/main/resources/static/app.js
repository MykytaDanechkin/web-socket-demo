let stompClient = null;
let currentSubscription = null;
let currentChatId = null;

$(function () {
    $(document).on("click", ".user-btn", function () {
        const $this = $(this);
        const targetUserId = $this.data("user-id");

        if ($this.hasClass("active")) {
            $this.removeClass("active");
            disconnectFromChat();
            return;
        }

        $(".user-btn").removeClass("active");
        $this.addClass("active");

        $.post("/api/chat/check-chat", { userId: targetUserId }, function (chatId) {
            connectToChat(chatId);
        });
    });


    $("#sendBtn").click(() => sendMessage());
    $("#msgInput").keypress((e) => {
        if (e.which === 13) sendMessage();
    });

    $("#loadHistoryBtn").click(() => loadFullHistory());
});

function connectToChat(chatId) {
    if (currentSubscription) currentSubscription.unsubscribe();
    currentChatId = chatId;
    $("#messages").empty();
    $("#loadHistoryBtn").show();

    if (!stompClient || !stompClient.connected) {
        stompClient = new StompJs.Client({
            webSocketFactory: () => new SockJS('/ws'),
            onConnect: () => {
                subscribeToChat(chatId);
                loadInitialHistory();
            }
        });
        stompClient.activate();
    } else {
        subscribeToChat(chatId);
        loadInitialHistory();
    }
}

function disconnectFromChat() {
    if (currentSubscription) {
        currentSubscription.unsubscribe();
        currentSubscription = null;
    }
    currentChatId = null;
    $("#messages").empty();
    $("#loadHistoryBtn").hide();
}


function subscribeToChat(chatId) {
    currentSubscription = stompClient.subscribe(`/topic/chat/${chatId}`, (msg) => {
        const message = JSON.parse(msg.body);
        showMessage(message);
    });
}

function sendMessage() {
    const text = $("#msgInput").val();
    if (!text.trim() || !currentChatId) return;
    stompClient.publish({
        destination: `/app/i`,
        body: JSON.stringify({ content: text, chatId: currentChatId })
    });
    $("#msgInput").val("");
}

function showMessage(msg) {
    const currentUserEmail = $("meta[name='userEmail']").attr("content");
    const isOwnMessage = msg.sendersEmail === currentUserEmail;
    const sideClass = isOwnMessage ? 'right' : 'left';
    const timestamp = formatTime(msg.timestamp);
    const html = `
        <div class="message ${sideClass}">
            <div><strong>${timestamp} ${msg.sendersEmail}</strong></div>
            <div>${msg.content}</div>
        </div>
    `;
    $("#messages").append(html);
    $("#messages").scrollTop($("#messages")[0].scrollHeight);
}

function showMessageAtTop(msg) {
    const currentUserEmail = $("meta[name='userEmail']").attr("content");
    const isOwnMessage = msg.sendersEmail === currentUserEmail;
    const sideClass = isOwnMessage ? 'right' : 'left';
    const timestamp = formatTime(msg.timestamp);
    const html = `
        <div class="message ${sideClass}">
            <div><strong>${timestamp} ${msg.sendersEmail}</strong></div>
            <div>${msg.content}</div>
        </div>
    `;
    $("#messages").prepend(html);
}

function loadInitialHistory() {
    $.get(`/api/chat/get-history?chatId=${currentChatId}`, function (data) {
        const messages = data.content || data;
        messages.forEach(showMessageAtTop);
        $("#messages").scrollTop($("#messages")[0].scrollHeight);
    });
}


function loadFullHistory() {
    $.get(`/api/chat/get-full-history?chatId=${currentChatId}`, function (data) {
        $("#messages").empty();
        const messages = data.content || data;
        messages.forEach(showMessageAtTop);
        $("#messages").scrollTop($("#messages")[0].scrollHeight);
    });
}

function formatTime(timestamp) {
    const date = new Date(timestamp);
    const hours = String(date.getHours()).padStart(2, '0');
    const mins = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${mins}`;
}
