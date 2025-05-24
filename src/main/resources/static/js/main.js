// Initialize all tooltips
document.addEventListener('DOMContentLoaded', function() {
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Initialize all toasts
    var toastElList = [].slice.call(document.querySelectorAll('.toast'));
    var toastList = toastElList.map(function(toastEl) {
        return new bootstrap.Toast(toastEl, {
            autohide: true,
            delay: 3000
        });
    });
    toastList.forEach(toast => toast.show());

    // Add confirm dialog to delete buttons
    document.querySelectorAll('[data-confirm]').forEach(function(element) {
        element.addEventListener('click', function(e) {
            if (!confirm(this.dataset.confirm)) {
                e.preventDefault();
            }
        });
    });

    // File input custom text
    document.querySelectorAll('.custom-file-input').forEach(function(input) {
        input.addEventListener('change', function(e) {
            var fileName = e.target.files[0].name;
            var next = e.target.nextElementSibling;
            next.innerText = fileName;
        });
    });

    // Form validation
    var forms = document.querySelectorAll('.needs-validation');
    Array.prototype.slice.call(forms).forEach(function(form) {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });

    // Password strength meter
    var passwordInputs = document.querySelectorAll('input[type="password"][data-password-strength]');
    passwordInputs.forEach(function(input) {
        input.addEventListener('input', function() {
            var password = this.value;
            var strength = 0;
            
            if (password.length >= 8) strength++;
            if (password.match(/[a-z]+/)) strength++;
            if (password.match(/[A-Z]+/)) strength++;
            if (password.match(/[0-9]+/)) strength++;
            if (password.match(/[^a-zA-Z0-9]+/)) strength++;

            var meter = this.nextElementSibling;
            if (meter && meter.classList.contains('password-strength-meter')) {
                meter.value = strength;
                
                var feedback = meter.nextElementSibling;
                if (feedback) {
                    var message = '';
                    switch(strength) {
                        case 0:
                        case 1:
                            message = 'Very weak';
                            feedback.className = 'text-danger';
                            break;
                        case 2:
                            message = 'Weak';
                            feedback.className = 'text-warning';
                            break;
                        case 3:
                            message = 'Medium';
                            feedback.className = 'text-info';
                            break;
                        case 4:
                            message = 'Strong';
                            feedback.className = 'text-success';
                            break;
                        case 5:
                            message = 'Very strong';
                            feedback.className = 'text-success';
                            break;
                    }
                    feedback.textContent = message;
                }
            }
        });
    });

    // Course search autocomplete
    var courseSearch = document.querySelector('#courseSearch');
    if (courseSearch) {
        courseSearch.addEventListener('input', debounce(function() {
            var query = this.value;
            if (query.length < 2) return;

            fetch('/api/courses/search?query=' + encodeURIComponent(query))
                .then(response => response.json())
                .then(data => {
                    var resultsContainer = document.querySelector('#searchResults');
                    resultsContainer.innerHTML = '';
                    
                    data.forEach(course => {
                        var div = document.createElement('div');
                        div.className = 'p-2 hover-bg-light border-bottom';
                        div.innerHTML = `
                            <a href="/courses/${course.id}" class="text-decoration-none text-dark">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <h6 class="mb-0">${course.title}</h6>
                                        <small class="text-muted">${course.instructor}</small>
                                    </div>
                                    <span class="badge bg-primary">${course.duration}</span>
                                </div>
                            </a>
                        `;
                        resultsContainer.appendChild(div);
                    });
                    
                    if (data.length === 0) {
                        resultsContainer.innerHTML = '<div class="p-2 text-muted">No results found</div>';
                    }
                });
        }, 300));
    }
});

// Debounce function
function debounce(func, wait) {
    var timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func.apply(this, args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Handle file uploads with progress
function handleFileUpload(fileInput, progressBar, url) {
    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append('file', file);

    const xhr = new XMLHttpRequest();
    xhr.open('POST', url, true);

    xhr.upload.onprogress = function(e) {
        if (e.lengthComputable) {
            const percentComplete = (e.loaded / e.total) * 100;
            progressBar.style.width = percentComplete + '%';
            progressBar.setAttribute('aria-valuenow', percentComplete);
        }
    };

    xhr.onload = function() {
        if (xhr.status === 200) {
            const response = JSON.parse(xhr.responseText);
            // Handle successful upload
            showToast('Success', 'File uploaded successfully!', 'success');
        } else {
            // Handle error
            showToast('Error', 'Upload failed. Please try again.', 'danger');
        }
    };

    xhr.send(formData);
}

// Show toast notification
function showToast(title, message, type = 'info') {
    const toastContainer = document.querySelector('.toast-container');
    if (!toastContainer) return;

    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');

    toast.innerHTML = `
        <div class="toast-header bg-${type} text-white">
            <strong class="me-auto">${title}</strong>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast"></button>
        </div>
        <div class="toast-body">
            ${message}
        </div>
    `;

    toastContainer.appendChild(toast);
    const bsToast = new bootstrap.Toast(toast, {
        autohide: true,
        delay: 3000
    });
    bsToast.show();

    toast.addEventListener('hidden.bs.toast', function() {
        toast.remove();
    });
} 