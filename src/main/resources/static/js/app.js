let stompClient = null;
let currentSubscription = null;
let currentChatId = null;
let targetEmail = null;

$(function () {
    initializeInboxConnection();

    $(document).on("click", ".user-btn", function () {
        const $this = $(this);
        const chatId = $this.data("chat-id");
        targetEmail = $this.data("user-email");

        if ($this.hasClass("active")) {
            $this.removeClass("active");
            $this.find(".last-message").css("display", "block");
            disconnectFromChat();
            return;
        }

        $(".user-btn").removeClass("active").find(".last-message").css("display", "block");

        $this.addClass("active");
        $this.find(".last-message").css("display", "none");

        connectToChat(chatId);
    });

    $("#sendBtn").click(sendMessage);
    $("#msgInput").keypress(e => {
        if (e.which === 13) sendMessage();
    });
});

function initializeInboxConnection() {
    const connectCallback = () => {
        subscribeToInbox();
    };

    if (!stompClient || !stompClient.connected) {
        stompClient = new StompJs.Client({
            webSocketFactory: () => new SockJS('/ws'),
            onConnect: connectCallback,
        });
        stompClient.activate();
    } else {
        connectCallback();
    }
}

function subscribeToInbox() {
    stompClient.subscribe(`/user/queue/inbox`, (msg) => {
        const message = JSON.parse(msg.body);
        updateLastMessageInChatPreview(message);
    });
}

function updateLastMessageInChatPreview(message) {
    const chatDiv = $(`.user-btn[data-chat-id="${message.chatId}"]`);
    if (!chatDiv.length) return;

    const currentUserEmail = $("meta[name='userEmail']").attr("content");
    const isUnseen = message.status === "UNSEEN";
    const isOwnMessage = message.sendersEmail === currentUserEmail;

    chatDiv.find(".last-message .last-sender").text(message.sendersEmail);
    chatDiv.find(".last-message .last-content").text(message.content);

    if (isUnseen && !isOwnMessage) {
        chatDiv.addClass("unseen");
    } else {
        chatDiv.removeClass("unseen");
    }

    if (!chatDiv.hasClass("active")) {
        chatDiv.find(".last-message").css("display", "block");
    }
}


function connectToChat(chatId) {
    if (currentSubscription) currentSubscription.unsubscribe();
    currentChatId = chatId;
    $("#messages").empty();

    const connectCallback = () => {
        subscribeToChat(chatId);
        loadInitialHistory();
    };

    if (!stompClient || !stompClient.connected) {
        stompClient = new StompJs.Client({
            webSocketFactory: () => new SockJS('/ws'),
            onConnect: connectCallback,
        });
        stompClient.activate();
    } else {
        connectCallback();
    }
}

function disconnectFromChat() {
    if (currentSubscription) {
        currentSubscription.unsubscribe();
        currentSubscription = null;
    }
    currentChatId = null;
    $("#messages").empty();
}

function subscribeToChat(chatId) {
    currentSubscription = stompClient.subscribe(`/topic/chat/${chatId}`, (msg) => {
        const message = JSON.parse(msg.body);

        if (Array.isArray(message)) {
            message.forEach(id => {
                const $msg = $(`.message[data-id='${id}']`);
                $msg.data("seen", true);
                if ($msg.data("own")) {
                    $msg.find(".status-label").text("SEEN");
                }
            });
            const chatDiv = $(`.user-btn[data-chat-id="${chatId}"]`);
            if (chatDiv.length) {

                chatDiv.removeClass("unseen");
            }
            return;
        }
        showMessageAtBottom(message);
        updateLastMessageInChatPreview(message);
        observeSeenMessages();
        scrollToBottom();
    });
}


function sendMessage() {
    const text = $("#msgInput").val().trim();
    if (!text || !currentChatId) return;

    stompClient.publish({
        destination: "/app/i",
        body: JSON.stringify({ content: text, chatId: currentChatId, getterEmail: targetEmail })
    });
    $("#msgInput").val("");
}

function showMessage(msg) {
    const html = renderMessageHtml(msg);
    $("#messages").prepend(html);
}

function showMessageAtBottom(msg) {
    const html = renderMessageHtml(msg);
    $("#messages").append(html);
}

function renderMessageHtml(msg) {
    const currentUserEmail = $("meta[name='userEmail']").attr("content");
    const isOwnMessage = msg.sendersEmail === currentUserEmail;
    const sideClass = isOwnMessage ? 'right' : 'left';
    const timestamp = formatTime(msg.timestamp);
    const seen = msg.status === "SEEN";

    return `
        <div class="message ${sideClass}" 
             data-id="${msg.id}" 
             data-seen="${seen}" 
             data-own="${isOwnMessage}">
            <div>
                <strong>${timestamp} ${msg.sendersEmail}</strong>
                ${isOwnMessage ? `<span class="status-label">${msg.status}</span>` : ``}
            </div>
            <div>${msg.content}</div>
        </div>
    `;
}


function loadInitialHistory() {
    $.get(`/api/chat/get-history?chatId=${currentChatId}`, function (data) {
        (data.content || data).forEach(showMessage);
        observeSeenMessages();
        scrollToFirstUnseen();
    });
}

function markMessagesAsSeen(ids) {
    if (!ids.length) return;

    $.ajax({
        url: "/api/chat/mark-seen",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ chatId: currentChatId, messageIds: ids }),
        success: function () {
            ids.forEach(id => {
                const $msg = $(`.message[data-id='${id}']`);
                $msg.data("seen", true);
                if ($msg.data("own")) {
                    $msg.find(".status-label").text("SEEN");
                }
            });
        }
    });
}

function observeSeenMessages() {
    const observer = new IntersectionObserver((entries) => {
        const seenMessageIds = [];

        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const $msg = $(entry.target);
                const messageId = $msg.data("id");
                const isSeen = $msg.data("seen");
                const isOwn = $msg.data("own");

                if (!isSeen && !isOwn) {
                    seenMessageIds.push(messageId);
                    $msg.data("seen", true);
                }
            }
        });

        if (seenMessageIds.length > 0) {
            markMessagesAsSeen(seenMessageIds);
        }
    }, { threshold: 1.0 });

    $(".message").each(function () {
        observer.observe(this);
    });
}

function scrollToFirstUnseen() {
    const $firstUnseen = $(".message").filter(function () {
        const $this = $(this);
        return !$this.data("seen") && !$this.data("own");
    }).first();

    if ($firstUnseen.length) {
        $firstUnseen[0].scrollIntoView({ behavior: 'auto', block: 'center' });
    } else {
        scrollToBottom();
    }
}

function formatTime(timestamp) {
    const date = new Date(timestamp);
    const hours = String(date.getHours()).padStart(2, '0');
    const mins = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${mins}`;
}

function scrollToBottom() {
    $("#messages").scrollTop($("#messages")[0].scrollHeight);
}
