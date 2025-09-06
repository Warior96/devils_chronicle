console.log("script loaded");

// funzione per tornare indietro
function goBack() {
    window.history.back();
}

// preview cover articolo
const coverImage = document.querySelector("#image");
const coverPreview = document.querySelector("#coverPreview");
const removeCoverBtn = document.querySelector("#removeCover");
let oldPreviewUrl = null;

if (coverImage) {
    coverImage.addEventListener("change", () => {
        if (oldPreviewUrl) {
            URL.revokeObjectURL(oldPreviewUrl);
        }
        if (coverImage.files && coverImage.files[0]) {
            oldPreviewUrl = URL.createObjectURL(coverImage.files[0]);
            coverPreview.src = oldPreviewUrl;
            coverPreview.classList.remove("d-none");
            removeCoverBtn.classList.remove("d-none");
        } else {
            coverPreview.src = "";
            coverPreview.classList.add("d-none");
            removeCoverBtn.classList.add("d-none");
        }
    });
};

if (removeCoverBtn) {
    removeCoverBtn.addEventListener("click", () => {
        if (oldPreviewUrl) {
            URL.revokeObjectURL(oldPreviewUrl);
            oldPreviewUrl = null;
        }
        coverImage.value = "";
        coverPreview.src = "";
        coverPreview.classList.add("d-none");
        removeCoverBtn.classList.add("d-none");
    });
};


// preview gallery articolo
const galleryInput = document.querySelector("#galleryImages");
const galleryPreview = document.querySelector("#galleryPreview");
const removeGalleryBtn = document.querySelector("#removeGallery");

if (galleryInput) {
    galleryInput.addEventListener("change", () => {
        galleryPreview.innerHTML = "";

        const files = Array.from(galleryInput.files);

        if (files.length > 0) {
            removeGalleryBtn.classList.remove("d-none");
        } else {
            removeGalleryBtn.classList.add("d-none");
        }

        files.forEach(file => {
            const wrapper = document.createElement("div");
            wrapper.classList.add("position-relative", "col-3", "mt-3", "px-2");

            const img = document.createElement("img");
            img.src = URL.createObjectURL(file);
            img.classList.add("img-fluid", "rounded", "border", "border-secondary-subtle", "p-1");
            img.onload = () => URL.revokeObjectURL(img.src);

            wrapper.appendChild(img);
            galleryPreview.appendChild(wrapper);
        });
    });
}

if (removeGalleryBtn) {
    removeGalleryBtn.addEventListener("click", () => {
        galleryInput.value = "";
        galleryPreview.innerHTML = "";
        removeGalleryBtn.classList.add("d-none");
    });
}


// Chat
document.addEventListener('DOMContentLoaded', function () {
    const askBtn = document.getElementById('ask-btn');
    const summarySection = document.getElementById('summary-section');
    const chatContainer = document.getElementById('chat-container');
    const chatInput = document.getElementById('chat-input');
    const sendBtn = document.getElementById('send-btn');
    const chatMessages = document.getElementById('chat-messages');
    const chatPlaceholder = document.getElementById('chat-placeholder');

    // Recupera l'ID dell'articolo da un attributo dati del body o da un meta tag
    const articleId = document.body.dataset.articleId;

    // Funzione per mostrare la chat e nascondere il riassunto
    function showChat() {
        if (summarySection) summarySection.style.display = 'none';
        chatContainer.style.display = 'block';
    }

    // Funzione per aggiungere un messaggio alla chat
    function addMessageToChat(sender, message) {
        const messageElement = document.createElement('div');
        messageElement.classList.add('px-4', 'py-3', 'mb-3');
        if (sender === 'user') {
            messageElement.classList.add('bg-red-dark', 'text-white', 'ms-auto', 'cus-round-user');
            messageElement.style.maxWidth = '70%';
            messageElement.textContent = `Tu: ${message}`;
        } else {
            messageElement.classList.add('bg-black', 'me-auto', 'cus-round-ai');
            messageElement.style.maxWidth = '70%';
            messageElement.textContent = `AI: ${message}`;
        }
        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
        if (chatPlaceholder) chatPlaceholder.style.display = 'none';
    }

    // Evento per il bottone "Fai una domanda"
    askBtn.addEventListener('click', () => {
        showChat();
        if (chatMessages.innerHTML === '') {
            addMessageToChat('ai', 'Ciao! Sono pronto a rispondere alle tue domande su questo articolo.');
        }
    });

    // Evento per il bottone "Invia" nella chat
    sendBtn.addEventListener('click', async () => {
        const question = chatInput.value;
        if (question.trim() === '') return;

        addMessageToChat('user', question);
        chatInput.value = '';
        chatInput.disabled = true;
        sendBtn.disabled = true;

        const loadingMessage = document.createElement('div');
        loadingMessage.classList.add('p-2', 'rounded', 'mb-2', 'bg-light');
        loadingMessage.style.maxWidth = '75%';
        loadingMessage.textContent = 'AI: ...';
        chatMessages.appendChild(loadingMessage);
        chatMessages.scrollTop = chatMessages.scrollHeight;

        try {
            const response = await fetch(`/articles/detail/${articleId}/ask`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ question: question })
            });

            chatMessages.removeChild(loadingMessage);

            if (response.ok) {
                const aiResponse = await response.text();
                addMessageToChat('ai', aiResponse);
            } else {
                const errorResponse = await response.text();
                addMessageToChat('ai', `Errore AI: ${errorResponse}`);
            }
        } catch (error) {
            chatMessages.removeChild(loadingMessage);
            addMessageToChat('ai', 'Errore nella comunicazione con l\'AI.');
            console.error('Errore:', error);
        } finally {
            chatInput.disabled = false;
            sendBtn.disabled = false;
        }
    });

    // Permette di inviare la domanda premendo Invio
    chatInput.addEventListener('keypress', (event) => {
        if (event.key === 'Enter') {
            sendBtn.click();
        }
    });
});


// Classifica - centra il Milan
document.addEventListener('DOMContentLoaded', function () {
    // Centra automaticamente il Milan nella classifica
    const milanRow = document.querySelector('.table-danger');
    if (milanRow) {
        const container = document.getElementById('standingsContainer');
        if (container) {
            const rowTop = milanRow.offsetTop;
            const containerHeight = container.clientHeight;
            const scrollTo = rowTop - (containerHeight / 2);
            container.scrollTop = Math.max(0, scrollTo);
        }
    }
});
