let stompClient = null;
let currentSubscription = null;
let currentChatId = null;
let targetEmail = null;

let currentHistoryPage = 1;
let initialPageSize = null;
let loadingHistory = false;
let allHistoryLoaded = false;


$(() => {
    initializeInboxConnection();
    initSend();
    initSearch();
    initChatSelection();
    initDisplayNameEdit();

    $(document).on("click", "#loadMoreBtn", loadMoreHistory);

    $("#messages").on("scroll", function () {
        if (this.scrollTop === 0) {
            loadMoreHistory();
        }
    });

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
    const currentUserId = Number($("meta[name='userId']").attr("content"));

    $searchInput.on("input", function () {
        const query = $(this).val().trim();
        if (query.length < 2) return $results.empty();

        $.get(`/api/user/find?tag=${encodeURIComponent(query)}`, users => {
            const html = users
                .filter(u => u.id !== currentUserId)
                .map(u => `<div class="search-user" data-user-id="${u.id}" data-display-name="${u.displayName}" data-user-tag="${u.tag}" data-email="${u.email}">${u.displayName} @${u.tag}</div>`)
                .join("");
            $results.html(html);
        }).fail(err => {
            console.error('Search failed', err);
        });
    });

    $(document).on("click", ".search-user", function () {
        const targetId = $(this).data("user-id");
        const targetDisplayName = $(this).data("display-name");
        const targetTag = $(this).data("user-tag");
        const email = $(this).data("email");

        const $existing = $(`.user-btn[data-user-id='${targetId}']`);
        if ($existing.length) {
            $existing.trigger("click");
            $("#globalUserSearch").val("");
            $("#globalUserResults").empty();
            return;
        } else {
            const tempChatId = `temp-${Date.now()}`;
            const html = renderChatPreview(tempChatId, targetDisplayName, targetTag, email, targetId);
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
        targetEmail = $this.data("user-email") || null;

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
                const currentUserId = Number($("meta[name='userId']").attr("content"));
                const target = chat.user1.id === currentUserId ? chat.user2 : chat.user1;
                const html = renderChatPreview(chat.id, target.displayName, target.tag, target.email, target.id);
                $("#chatList").prepend(html);
                updateLastMessageInChatPreview(message);
            }).fail(err => {
                console.error('Failed to fetch chat', err);
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
            currentHistoryPage = 1;
            allHistoryLoaded = false;
            loadingHistory = false;
            initialPageSize = null;
            $("#loadMoreIndicator").removeClass("hidden");
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
    loadingHistory = false;
    allHistoryLoaded = false;
    $("#messages").children().remove();
    $("#loadMoreIndicator").addClass("hidden");
}

function loadMoreHistory() {
    if (loadingHistory || allHistoryLoaded || !initialPageSize || !currentChatId) return;
    loadingHistory = true;

    const $messages = $("#messages");
    const prevScrollHeight = $messages[0].scrollHeight;
    
    const $firstMsg = $("#messages .message").first();
    const currentOldestTs = $firstMsg.length ? Number($firstMsg.data("ts")) : Number.MAX_SAFE_INTEGER;

    $.get(`/api/chat/get-full-history?chatId=${currentChatId}&pageSize=${initialPageSize}&page=${currentHistoryPage}`, data => {
        if (!Array.isArray(data) || data.length === 0) {
            allHistoryLoaded = true;
            $("#loadMoreIndicator").addClass("hidden");
            return;
        }
        const existingIds = new Set($("#messages .message").map((_, el) => $(el).data("id")).get());
        const toPrepend = data.filter(m => {
            if (!m || m.id == null || !m.timestamp) return false;
            const ts = new Date(m.timestamp).getTime();
            return !existingIds.has(m.id) && ts < currentOldestTs;
        });
        
        toPrepend.forEach(msg => {
            $("#messages").prepend(renderMessageHtml(msg));
        });

        currentHistoryPage++;
        observeSeenMessages();

        const newScrollHeight = $messages[0].scrollHeight;
        const delta = newScrollHeight - prevScrollHeight;
        $messages.scrollTop(delta);
    }).always(() => {
        loadingHistory = false;
    });
}

function subscribeToChat(chatId) {
    currentSubscription = stompClient.subscribe(`/topic/chat/${chatId}`, msg => {
        const payload = JSON.parse(msg.body);

        if (!currentChatId || String(currentChatId) !== String(chatId)) return;

        if (Array.isArray(payload)) {
            payload.forEach(id => {
                const $msg = $(`.message[data-id='${id}']`);
                $msg.data("seen", true);
                if ($msg.data("own")) $msg.find(".status-label").text("SEEN");
            });
            $(`.user-btn[data-chat-id="${chatId}"]`).removeClass("unseen");
            return;
        }

        const message = payload;
        if (!message || message.content == null || message.senderId == null) return;

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
        const currentUserDisplayName = $("meta[name='userDisplayName']").attr("content");
        const currentUserTag = $("meta[name='userTag']").attr("content");

        $.post({
            url: `/api/chat/create-with-message`,
            contentType: "application/json",
            data: JSON.stringify({
                currentUserId,
                targetUserId,
                initialMessage: text,
                targetEmail: $tempChat.data('user-email') || null
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
                    sendersEmail: $("meta[name='userEmail']").attr("content"),
                    timestamp: new Date().toISOString()
                });
            },
            error: err => {
                console.error('Failed to create chat with initial message', err);
            }
        });
        return;
    }

    const sendersId = Number($("meta[name='userId']").attr("content"));
    const target = targetEmail || $(".user-btn.active").data("user-email") || null;
    if (!target) {
        console.error('Target email is missing for this chat');
    }
    stompClient.publish({
        destination: "/app/i",
        body: JSON.stringify({content: text, chatId: currentChatId, sendersId, targetEmail: target})
    });
    $("#msgInput").val("");
}

function loadInitialHistory() {
    $.get(`/api/chat/get-history?chatId=${currentChatId}`, data => {
        const messages = data.content || data;
        initialPageSize = messages.length;

        const sorted = [...messages].sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));

        $("#messages").empty().prepend($("#loadMoreIndicator"));
        sorted.forEach(msg => {
            $("#messages").append(renderMessageHtml(msg));
        });

        observeSeenMessages();
        scrollToFirstUnseen();
    });
}

function showMessageAtBottom(msg) {
    if (!msg || msg.content == null || msg.senderId == null) return;
    $("#messages").append(renderMessageHtml(msg));
}

function renderMessageHtml(msg) {
    const currentUserId = Number($("meta[name='userId']").attr("content"));
    const senderId = msg.senderId;
    const content = msg.content ?? '';
    const ts = msg.timestamp ? new Date(msg.timestamp) : new Date();
    const side = senderId === currentUserId ? 'right' : 'left';
    const seen = msg.status === "SEEN";
    const time = formatTime(ts);

    let senderLabel = 'You';
    if (senderId !== currentUserId) {
        const $activeTitle = $(".user-btn.active .chat-title");
        const title = $activeTitle.length ? $activeTitle.text().trim() : '';
        senderLabel = title || 'User';
    }

    return `
        <div class="message ${side}" data-id="${msg.id}" data-seen="${seen}" data-own="${senderId === currentUserId}" data-ts="${ts.getTime()}">
            <div>
                <strong>${time}</strong>
                <span class="sender-label">${senderLabel}</span>
                ${senderId === currentUserId ? `<span class="status-label">${msg.status ?? ''}</span>` : ``}
            </div>
            <div>${content}</div>
        </div>
    `;
}

function renderChatPreview(chatId, displayName, tag, email, userId) {
    return `
        <div class="user-btn unseen" data-chat-id="${chatId}" data-user-email="${email}" data-user-id="${userId}" data-last-timestamp="0">
            <div class="chat-title">${displayName} @${tag}</div>
            <div class="last-message" style="display: none;">
                <div class="last-sender"></div>
                <div class="last-content"></div>
            </div>
        </div>
    `;
}

function updateLastMessageInChatPreview(message) {
    const $chatDiv = $(`.user-btn[data-chat-id="${message.chatId}"]`);
    if (!$chatDiv.length || !message) return;

    const currentUserId = Number($("meta[name='userId']").attr("content"));
    const isUnseen = message.status === "UNSEEN";
    const isOwn = message.senderId === currentUserId;

    const title = $chatDiv.find('.chat-title').text().trim();
    const label = isOwn ? 'You' : (title || 'User');

    $chatDiv.find(".last-sender").text(label);
    $chatDiv.find(".last-content").text(message.content ?? '');
    const ts = message.timestamp ? new Date(message.timestamp).getTime() : Date.now();
    $chatDiv.attr("data-last-timestamp", String(ts));

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
        threshold: 0.01
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

function initDisplayNameEdit() {
    $(document).on('click', '#changeNameBtn', () => {
        const current = $("meta[name='userDisplayName']").attr("content") || '';
        const newName = prompt('Enter new display name', current);
        if (newName == null) return;
        const trimmed = newName.trim();
        if (!trimmed) return;

        $.ajax({
            url: '/api/user/display-name',
            method: 'POST',
            contentType: 'text/plain; charset=UTF-8',
            data: trimmed,
        }).done(() => {
            $("meta[name='userDisplayName']").attr("content", trimmed);
            const userTag = $("meta[name='userTag']").attr("content") || '';
            $('#userIdentity').text(`${trimmed} @${userTag}`);
        }).fail(err => {
            console.error('Failed to update display name', err);
            alert('Failed to update display name');
        });
    });
}

function formatTime(ts) {
    const d = new Date(ts);
    return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}
