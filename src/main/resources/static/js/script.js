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