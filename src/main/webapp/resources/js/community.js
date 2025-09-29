document.addEventListener('DOMContentLoaded', () => {
    updateAuthButtons();

    (async () => {
        try {
            const status = await appUtils.requireLogin();
            initializeCommunity(status.username);
        } catch (error) {
            console.error(error);
        }
    })();
});

function initializeCommunity(username) {
    const postForm = document.getElementById('post-form');
    const postsContainer = document.getElementById('posts-container');
    const editPostForm = document.getElementById('edit-post-form');
    const saveEditBtn = document.getElementById('save-edit-btn');
    const editModal = new bootstrap.Modal(document.getElementById('editModal'));
    const adminUsername = 'admin';

    async function loadPosts() {
        try {
            const posts = await appUtils.apiFetch('/api/posts', { method: 'GET', headers: { 'Content-Type': 'application/json' } });
            const followData = await appUtils.apiFetch('/api/follows', { method: 'GET', headers: { 'Content-Type': 'application/json' } });
            const myFollows = followData[username]?.following || [];

            postsContainer.innerHTML = '';
            if (!posts.length) {
                postsContainer.innerHTML = '<p class="text-center text-muted">게시글이 없습니다. 첫 게시글을 작성해 보세요!</p>';
                return;
            }

            posts.forEach(post => {
                const isMyPost = post.author === username;
                const isAdmin = username === adminUsername;
                const isFollowing = myFollows.includes(post.author);

                const postEl = document.createElement('div');
                postEl.className = 'card mb-3';
                postEl.innerHTML = `
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <h5 class="card-title">${post.title} <span class="badge bg-secondary">${post.category}</span></h5>
                            <div class="d-flex">
                                <span class="text-muted small me-2">${post.author}</span>
                                <span class="text-muted small">${post.date || ''}</span>
                            </div>
                        </div>
                        <p class="card-text">${post.content}</p>
                        <hr>
                        <div class="d-flex justify-content-between align-items-center">
                            <div class="d-flex">
                                <button class="btn btn-sm btn-outline-primary like-btn" data-id="${post.id}">❤️ 좋아요 ${post.likes || 0}</button>
                            </div>
                            <div class="d-flex">
                                ${isMyPost ? `
                                    <button class="btn btn-sm btn-outline-secondary me-2 edit-post-btn" data-id="${post.id}">수정</button>
                                    <button class="btn btn-sm btn-outline-danger delete-post-btn" data-id="${post.id}">삭제</button>
                                ` : ''}
                                ${!isMyPost && !isAdmin ? (isFollowing ? `
                                    <button class="btn btn-sm btn-outline-primary unfollow-btn" data-author="${post.author}">팔로잉</button>
                                ` : `
                                    <button class="btn btn-sm btn-primary follow-btn" data-author="${post.author}">팔로우</button>
                                `) : ''}
                                ${isAdmin && !isMyPost ? `
                                    <button class="btn btn-sm btn-outline-danger delete-post-btn ms-2" data-id="${post.id}">삭제</button>
                                ` : ''}
                            </div>
                        </div>
                        <div class="mt-3">
                            <strong>댓글</strong>
                            <div class="comments-container mt-2" data-post-id="${post.id}"></div>
                            <form class="comment-form mt-2" data-post-id="${post.id}">
                                <div class="input-group">
                                    <input type="text" class="form-control" placeholder="댓글을 입력하세요..." required>
                                    <button class="btn btn-outline-secondary" type="submit">작성</button>
                                </div>
                            </form>
                        </div>
                    </div>
                `;
                postsContainer.appendChild(postEl);
                loadComments(post.id, post.comments || []);
            });

            addEventListeners();
        } catch (error) {
            console.error('게시글 불러오기 중 오류 발생:', error);
            postsContainer.innerHTML = '<p class="text-danger">게시글 데이터를 불러오는 데 실패했습니다.</p>';
        }
    }

    function loadComments(postId, comments) {
        const commentsContainer = document.querySelector(`.comments-container[data-post-id="${postId}"]`);
        commentsContainer.innerHTML = '';
        comments.forEach(comment => {
            const commentEl = document.createElement('div');
            commentEl.className = 'd-flex justify-content-between small text-muted';
            commentEl.innerHTML = `
                <span><strong>${comment.author}</strong>: ${comment.content}</span>
                <span>${comment.date || ''}</span>
            `;
            commentsContainer.appendChild(commentEl);
        });
    }

    postForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const newPost = {
            title: document.getElementById('post-title').value,
            category: document.getElementById('post-category').value,
            content: document.getElementById('post-content').value,
            date: new Date().toLocaleDateString('ko-KR')
        };
        appUtils.apiFetch('/api/posts', {
            method: 'POST',
            body: newPost
        }).then(() => {
            alert('게시글이 성공적으로 작성되었습니다.');
            postForm.reset();
            loadPosts();
        }).catch(error => {
            alert('게시글 작성 실패: ' + error.message);
        });
    });

    function addEventListeners() {
        document.querySelectorAll('.like-btn').forEach(button => {
            button.addEventListener('click', (e) => {
                const postId = parseInt(e.target.dataset.id, 10);
                toggleLike(postId);
            });
        });

        document.querySelectorAll('.comment-form').forEach(form => {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                const postId = parseInt(e.target.dataset.postId, 10);
                const commentInput = e.target.querySelector('input');
                const commentContent = commentInput.value;
                if (commentContent.trim()) {
                    addComment(postId, commentContent);
                    commentInput.value = '';
                }
            });
        });

        document.querySelectorAll('.delete-post-btn').forEach(button => {
            button.addEventListener('click', (e) => {
                const postId = parseInt(e.target.dataset.id, 10);
                if (confirm('정말 삭제하시겠습니까?')) {
                    deletePost(postId);
                }
            });
        });

        document.querySelectorAll('.edit-post-btn').forEach(button => {
            button.addEventListener('click', (e) => {
                const postId = parseInt(e.target.dataset.id, 10);
                getPostById(postId).then(post => {
                    if (post) {
                        document.getElementById('edit-post-id').value = post.id;
                        document.getElementById('edit-title').value = post.title;
                        document.getElementById('edit-category').value = post.category;
                        document.getElementById('edit-content').value = post.content;
                        editModal.show();
                    }
                });
            });
        });

        document.querySelectorAll('.follow-btn').forEach(button => {
            button.addEventListener('click', (e) => {
                const followee = e.target.dataset.author;
                toggleFollow(followee, true);
            });
        });

        document.querySelectorAll('.unfollow-btn').forEach(button => {
            button.addEventListener('click', (e) => {
                const followee = e.target.dataset.author;
                toggleFollow(followee, false);
            });
        });
    }

    async function getPostById(id) {
        const posts = await appUtils.apiFetch('/api/posts', { method: 'GET', headers: { 'Content-Type': 'application/json' } });
        return posts.find(p => p.id === id);
    }

    saveEditBtn.addEventListener('click', () => {
        const id = parseInt(document.getElementById('edit-post-id').value, 10);
        const updatedPost = {
            title: document.getElementById('edit-title').value,
            category: document.getElementById('edit-category').value,
            content: document.getElementById('edit-content').value,
            date: new Date().toLocaleDateString('ko-KR')
        };
        updatePost(id, updatedPost);
    });

    function updatePost(id, updatedPost) {
        appUtils.apiFetch(`/api/posts/${id}`, {
            method: 'PUT',
            body: updatedPost
        }).then(() => {
            alert('게시글이 성공적으로 수정되었습니다.');
            editModal.hide();
            loadPosts();
        }).catch(error => {
            alert('게시글 수정 실패: ' + error.message);
        });
    }

    function deletePost(id) {
        appUtils.apiFetch(`/api/posts/${id}`, {
            method: 'DELETE'
        }).then(() => {
            alert('게시글이 성공적으로 삭제되었습니다.');
            loadPosts();
        }).catch(error => {
            alert('게시글 삭제 실패: ' + error.message);
        });
    }

    function toggleFollow(followee, follow) {
        appUtils.apiFetch('/api/follows', {
            method: 'POST',
            body: { followee, action: follow ? 'follow' : 'unfollow' }
        }).then(() => {
            alert(follow ? `${followee}님을 팔로우했습니다.` : `${followee}님을 팔로잉 취소했습니다.`);
            loadPosts();
        }).catch(error => {
            alert('팔로우/언팔로우 실패: ' + error.message);
        });
    }

    function toggleLike(id) {
        appUtils.apiFetch(`/api/posts/${id}/like`, {
            method: 'POST'
        }).then(() => {
            loadPosts();
        }).catch(error => {
            alert('좋아요 변경 실패: ' + error.message);
        });
    }

    function addComment(id, content) {
        const newComment = {
            content,
            date: new Date().toLocaleDateString('ko-KR')
        };
        appUtils.apiFetch(`/api/posts/${id}/comment`, {
            method: 'POST',
            body: newComment
        }).then(() => {
            loadPosts();
        }).catch(error => {
            alert('댓글 작성 실패: ' + error.message);
        });
    }

    loadPosts();
}
