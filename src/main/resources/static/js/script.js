console.log("script loaded");

// funzione per tornare indietro
function goBack() {
    window.history.back();
}

// preview cover articolo
const coverImage = document.querySelector("#image");
const coverPreview = document.querySelector("#coverPreview");
let oldPreviewUrl = null;

coverImage.addEventListener("change", () => {
    if (oldPreviewUrl) {
        URL.revokeObjectURL(oldPreviewUrl);
    }
    if (coverImage.files && coverImage.files[0]) {
        oldPreviewUrl = URL.createObjectURL(coverImage.files[0]);
        coverPreview.src = oldPreviewUrl;
        coverPreview.classList.remove("d-none");
    }
});


// preview gallery articolo
const galleryInput = document.querySelector("#galleryImages");
const galleryPreview = document.querySelector("#galleryPreview");

galleryInput.addEventListener("change", () => {
    galleryPreview.innerHTML = "";

    const files = Array.from(galleryInput.files);

    files.forEach(file => {
        const img = document.createElement("img");
        img.src = URL.createObjectURL(file);
        img.classList.add("col-3", "mt-3", "px-2");
        img.onload = () => URL.revokeObjectURL(img.src);

        galleryPreview.appendChild(img);
    });
});
