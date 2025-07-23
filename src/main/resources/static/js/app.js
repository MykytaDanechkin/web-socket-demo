let stompClient = null;
let currentSubscription = null;
let currentChatId = null;
let targetEmail = null;

$(() => {
    initializeInboxConnection();
    initSend();
    initSearch();
    initChatSelection();
});

function initSend() {
    $("#sendBtn").click(sendMessage);
    $("#msgInput").on("keydown", e => {
        if (e.key === "Enter") {
            e.preventDefault();
            sendMessage();
        }
    });

}

function initSearch() {
    const $searchInput = $("#globalUserSearch");
    const $results = $("#globalUserResults");
    const currentEmail = $("meta[name='userEmail']").attr("content");

    $searchInput.on("input", function () {
        const query = $(this).val().trim();
        if (query.length < 4) return $results.empty();

        $.get(`/api/user/find?email=${encodeURIComponent(query)}`, users => {
            const html = users
                .filter(u => u.email !== currentEmail)
                .map(u => `<div class="search-user" data-user-id="${u.id}" data-user-email="${u.email}">${u.email}</div>`)
                .join("");
            $results.html(html);
        });
    });

    $(document).on("click", ".search-user", function () {
        const targetId = $(this).data("user-id");
        const targetEmailVal = $(this).data("user-email");
        const currentUserId = $("meta[name='userId']").attr("content");

        const $existing = $(`.user-btn[data-user-email='${targetEmailVal}']`);
        if ($existing.length) {
            $existing.trigger("click");
            $("#globalUserSearch").val("");
            $("#globalUserResults").empty();
            return;
        } else {
            const tempChatId = `temp-${Date.now()}`;
            const html = renderChatPreview(tempChatId, targetEmailVal, targetId);
            $("#chatList").prepend(html);
            $(`.user-btn[data-chat-id='${tempChatId}']`).trigger("click");
        }
        $searchInput.val("");
        $results.empty();
    });
}

function initChatSelection() {
    $(document).on("click", ".user-btn", function () {
        const $this = $(this);
        const chatId = $this.data("chat-id");
        targetEmail = $this.data("user-email");

        const isActive = $this.hasClass("active");

        $(".user-btn").removeClass("active").find(".last-message").show();

        if (isActive) {
            disconnectFromChat();
            return;
        }

        $this.addClass("active").find(".last-message").hide();
        connectToChat(chatId);
    });
}

function initializeInboxConnection() {
    const connectCallback = () => subscribeToInbox();

    if (!stompClient || !stompClient.connected) {
        stompClient = new StompJs.Client({
            webSocketFactory: () => new SockJS('/ws'),
            onConnect: connectCallback
        });
        stompClient.activate();
    } else {
        connectCallback();
    }
}

function subscribeToInbox() {
    stompClient.subscribe(`/user/queue/inbox`, msg => {
        const message = JSON.parse(msg.body);
        const chatId = message.chatId;
        const $chatDiv = $(`.user-btn[data-chat-id="${chatId}"]`);

        if ($chatDiv.length) {
            updateLastMessageInChatPreview(message);
        } else {
            $.get(`/api/chat/get?id=${chatId}`, chat => {
                const currentEmail = $("meta[name='userEmail']").attr("content");
                const target = chat.user1.email === currentEmail ? chat.user2 : chat.user1;
                const html = renderChatPreview(chat.id, target.email);
                $("#chatList").prepend(html);
                updateLastMessageInChatPreview(message);
            });
        }
    });
}

function connectToChat(chatId) {
    if (currentSubscription) currentSubscription.unsubscribe();
    currentChatId = chatId;
    $("#messages").empty();

    const connectCallback = () => {
        subscribeToChat(chatId);
        if (typeof chatId !== 'string' || !chatId.startsWith('temp-')) {
            loadInitialHistory();
        }
    };
    if (!stompClient || !stompClient.connected) {
        stompClient = new StompJs.Client({
            webSocketFactory: () => new SockJS('/ws'),
            onConnect: connectCallback
        });
        stompClient.activate();
    } else {
        connectCallback();
    }
}


function disconnectFromChat() {
    if (currentSubscription) currentSubscription.unsubscribe();
    currentSubscription = null;
    currentChatId = null;
    $("#messages").children().remove();
}


function subscribeToChat(chatId) {
    currentSubscription = stompClient.subscribe(`/topic/chat/${chatId}`, msg => {
        const message = JSON.parse(msg.body);

        if (Array.isArray(message)) {
            message.forEach(id => {
                const $msg = $(`.message[data-id='${id}']`);
                $msg.data("seen", true);
                if ($msg.data("own")) $msg.find(".status-label").text("SEEN");
            });

            $(`.user-btn[data-chat-id="${chatId}"]`).removeClass("unseen");
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

    if (typeof currentChatId === 'string' && currentChatId.startsWith('temp-')) {
        const $tempChat = $(`.user-btn[data-chat-id='${currentChatId}']`);
        const targetUserId = $tempChat.data('user-id');

        const currentUserId = $("meta[name='userId']").attr("content");
        const currentUserEmail = $("meta[name='userEmail']").attr("content");

        $.post({
            url: `/api/chat/create-with-message`,
            contentType: "application/json",
            data: JSON.stringify({
                currentUserId,
                targetUserId,
                targetEmail,
                initialMessage: text,
            }),
            success: newChat => {
                $tempChat.attr('data-chat-id', newChat.id)
                    .removeData("chat-id")
                    .data("chat-id", newChat.id);
                currentChatId = newChat.id;
                $("#msgInput").val("");
                connectToChat(newChat.id);

                updateLastMessageInChatPreview({
                    chatId: newChat.id,
                    content: text,
                    sendersEmail: currentUserEmail,
                    timestamp: new Date().toISOString()
                });
            }
        });
        return;
    }

    stompClient.publish({
        destination: "/app/i",
        body: JSON.stringify({content: text, chatId: currentChatId, getterEmail: targetEmail})
    });
    $("#msgInput").val("");
}



function loadInitialHistory() {
    $.get(`/api/chat/get-history?chatId=${currentChatId}`, data => {
        (data.content || data).forEach(showMessage);
        observeSeenMessages();
        scrollToFirstUnseen();
    });
}

function showMessage(msg) {
    $("#messages").prepend(renderMessageHtml(msg));
}

function showMessageAtBottom(msg) {
    $("#messages").append(renderMessageHtml(msg));
}

function renderMessageHtml(msg) {
    const currentEmail = $("meta[name='userEmail']").attr("content");
    const side = msg.sendersEmail === currentEmail ? 'right' : 'left';
    const seen = msg.status === "SEEN";
    const time = formatTime(msg.timestamp);

    return `
        <div class="message ${side}" data-id="${msg.id}" data-seen="${seen}" data-own="${msg.sendersEmail === currentEmail}">
            <div>
                <strong>${time} ${msg.sendersEmail}</strong>
                ${msg.sendersEmail === currentEmail ? `<span class="status-label">${msg.status}</span>` : ``}
            </div>
            <div>${msg.content}</div>
        </div>
    `;
}

function renderChatPreview(chatId, email, userId) {
    return `
        <div class="user-btn unseen" data-chat-id="${chatId}" data-user-email="${email}" data-user-id="${userId}" data-last-timestamp="0">
            <div class="chat-email">${email}</div>
            <div class="last-message" style="display: none;">
                <div class="last-sender"></div>
                <div class="last-content"></div>
            </div>
        </div>
    `;
}

function updateLastMessageInChatPreview(message) {
    const $chatDiv = $(`.user-btn[data-chat-id="${message.chatId}"]`);
    if (!$chatDiv.length) return;

    const currentEmail = $("meta[name='userEmail']").attr("content");
    const isUnseen = message.status === "UNSEEN";
    const isOwn = message.sendersEmail === currentEmail;

    $chatDiv.find(".last-sender").text(message.sendersEmail);
    $chatDiv.find(".last-content").text(message.content);
    $chatDiv.attr("data-last-timestamp", message.timestamp);

    $chatDiv.toggleClass("unseen", isUnseen && !isOwn);
    if (!$chatDiv.hasClass("active")) $chatDiv.find(".last-message").show();

    moveChatToTop($chatDiv);
}

function moveChatToTop($chat) {
    const ts = Number($chat.attr("data-last-timestamp") || 0);
    const $chatList = $("#chatList");

    $chat.detach();
    const $chats = $chatList.children(".user-btn");

    let inserted = false;
    $chats.each(function () {
        if (ts >= Number($(this).attr("data-last-timestamp") || 0)) {
            $(this).before($chat);
            inserted = true;
            return false;
        }
    });

    if (!inserted) $chatList.prepend($chat);
}

function observeSeenMessages() {
    const observer = new IntersectionObserver(entries => {
        const ids = entries
            .filter(e => e.isIntersecting)
            .map(e => $(e.target))
            .filter($m => !$m.data("seen") && !$m.data("own"))
            .map($m => {
                $m.data("seen", true);
                return $m.data("id");
            });

        if (ids.length > 0) markMessagesAsSeen(ids);
    }, {
        root: document.querySelector("#messages"),
        threshold: 0.3
    });

    $(".message").each(function () {
        observer.observe(this);
    });
}

function markMessagesAsSeen(ids) {
    $.post({
        url: "/api/chat/mark-seen",
        contentType: "application/json",
        data: JSON.stringify({chatId: currentChatId, messageIds: ids}),
        success: () => {
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

function scrollToBottom() {
    $("#messages").scrollTop($("#messages")[0].scrollHeight);
}

function scrollToFirstUnseen() {
    const $msg = $(".message").filter((_, el) => {
        const $el = $(el);
        return !$el.data("seen") && !$el.data("own");
    }).first();

    $msg.length ? $msg[0].scrollIntoView({behavior: "auto", block: "center"}) : scrollToBottom();
}

function formatTime(ts) {
    const d = new Date(ts);
    return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}
