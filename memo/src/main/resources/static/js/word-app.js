const BASE_URL = '/api/words';
let currentExamWords = []; // 현재 시험 중인 단어들 스냅샷

window.onload = function() {
    loadDates();
};

// 1. 메인 홈: 날짜 리스트 가져오기 (순수 문자열 배열 형태 처리)
function loadDates() {
    fetch(`${BASE_URL}/dates`)
        .then(res => res.json())
        .then(dates => {
            const dateListDiv = document.getElementById('date-list');
            dateListDiv.innerHTML = '';

            if(!dates || dates.length === 0) {
                dateListDiv.innerHTML = '<p class="text-center text-muted">데이터가 없습니다. 노션 동기화를 진행하세요.</p>';
                return;
            }

            dates.forEach(date => {
                const btn = document.createElement('button');
                btn.className = 'list-group-item list-group-item-action d-flex justify-content-between align-items-center';
                btn.innerHTML = `<span><i class="bi bi-journal-bookmark text-primary me-2"></i><strong>${date}</strong> 학습 단어장</span> <span class="badge bg-primary rounded-pill">보기</span>`;
                btn.onclick = () => loadDateWords(date);
                dateListDiv.appendChild(btn);
            });
        }).catch(err => console.error("날짜 로드 실패:", err));
}

// 2. 날짜 클릭 시: 단어 학습 리스트 조회
function loadDateWords(date) {
    fetch(`${BASE_URL}/date/${date}`)
        .then(res => res.json())
        .then(words => {
            document.getElementById('selected-date-title').innerText = `📅 ${date} 단어장 (${words.length}개)`;
            document.getElementById('btn-date-exam').onclick = () => startDateExam(date);

            const tbody = document.getElementById('word-table-body');
            tbody.innerHTML = '';

            words.forEach(w => {
                // 뜻에 포함된 줄바꿈 기호 처리
                const visualMeaning = w.meaning ? w.meaning.replace(/\n/g, '<br>') : '';
                tbody.innerHTML += `<tr>
                    <td class="fw-bold text-primary">${w.word}</td>
                    <td class="text-start ps-4">${visualMeaning}</td>
                    <td><span class="badge bg-danger">${w.wrongCount}회</span></td>
                </tr>`;
            });
            switchView('study-view');
        }).catch(err => console.error("단어 로드 실패:", err));
}

// 3. 시험 준비 단계 (특정 날짜 / 전체 80개 공통)
function startDateExam(date) {
    fetch(`${BASE_URL}/exam/date/${date}`)
        .then(res => res.json())
        .then(words => generateExamPaper(words, `📝 ${date} 단어 시험지`));
}

function startRandom80Exam() {
    fetch(`${BASE_URL}/exam/random80`)
        .then(res => res.json())
        .then(words => generateExamPaper(words, `⚡ 전체 랜덤 80 챌린지 시험지`));
}

// 4. 통째로 푸는 시험지 생성 로직
function generateExamPaper(words, title) {
    if(!words || words.length === 0) {
        alert("시험을 치를 단어가 데이터베이스에 없습니다.");
        return;
    }
    currentExamWords = words; // 스냅샷 저장
    document.getElementById('exam-title').innerText = title;

    const paperBody = document.getElementById('exam-paper-body');
    paperBody.innerHTML = '';

    words.forEach((w, index) => {
        const visualMeaning = w.meaning ? w.meaning.replace(/\n/g, '<br>') : '';
        const row = `
            <tr>
                <td><strong>${index + 1}</strong></td>
                <td class="text-start fw-bold text-dark fs-5 ps-4">${w.word}</td>
                <td>
                    <input type="text" class="form-control text-center text-input" placeholder="뜻을 입력하세요.">
                </td>
                <td>
                    <div class="d-flex align-items-center justify-content-center gap-3">
                        <button class="btn btn-sm btn-outline-secondary" onclick="toggleAnswerVisibility(${index}, this)">정답 보기</button>
                        <span id="ans-text-${index}" class="fw-bold text-success d-none text-start">${visualMeaning}</span>
                        <div class="form-check">
                            <input class="form-check-input border-danger wrong-checkbox" type="checkbox" value="${w.id}" id="check-${index}" style="transform: scale(1.3);">
                            <label class="form-check-label text-danger small fw-bold" for="check-${index}">틀림 ❌</label>
                        </div>
                    </div>
                </td>
            </tr>`;
        paperBody.innerHTML += row;
    });

    switchView('exam-view');
}

// 5. 시험지 내에서 개별 정답 토글 기능
function toggleAnswerVisibility(index, btnElement) {
    const ansSpan = document.getElementById(`ans-text-${index}`);
    if(ansSpan.classList.contains('d-none')) {
        ansSpan.classList.remove('d-none');
        btnElement.innerText = "숨기기";
    } else {
        ansSpan.classList.add('d-none');
        btnElement.innerText = "정답 보기";
    }
}

// 6. 유저가 채점 후 최종 제출할 때 틀린 것만 수집해서 MySQL 전송
function submitSelfGrading() {
    const checkboxes = document.querySelectorAll('.wrong-checkbox');
    const wrongWordIds = [];

    checkboxes.forEach(cb => {
        if(cb.checked) {
            wrongWordIds.push(Number(cb.value)); // 체크박스에 심어둔 단어 ID 추출
        }
    });

    if(!confirm(`총 ${currentExamWords.length}문제 중 ${wrongWordIds.length}개를 오답 처리하고 결과를 반영하시겠습니까?`)) {
        return;
    }

    if (wrongWordIds.length > 0) {
        fetch(`${BASE_URL}/exam/wrong`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(wrongWordIds)
        })
        .then(res => {
            if(res.ok) {
                alert("오답 카운트가 MySQL 데이터베이스에 누적 반영되었습니다! 🎉");
                showHome();
            } else {
                alert("서버 전송 중 오류가 발생했습니다.");
            }
        }).catch(err => console.error("오답 데이터 전송 실패:", err));
    } else {
        alert("와우! 다 맞추셨네요! 💯");
        showHome();
    }
}

// 7. 틀린 단어 모음 (오답 노트) 데이터 바인딩
function loadWrongNotebook() {
    fetch(`${BASE_URL}/wrongWords`)
        .then(res => res.json())
        .then(words => {
            const cardHeader = document.getElementById('study-card-header');
            if (cardHeader) {
                cardHeader.className = 'card-header bg-danger text-white d-flex justify-content-between align-items-center';
            }

            document.getElementById('selected-date-title').innerText = `📕 틀린 단어 모음 오답 노트 (${words.length}개)`;

            const examBtn = document.getElementById('btn-date-exam');
            if (examBtn) examBtn.classList.add('d-none');

            const tbody = document.getElementById('word-table-body');
            tbody.innerHTML = '';

            if(!words || words.length === 0) {
                tbody.innerHTML = `<tr><td colspan="3" class="text-muted py-4">아직 틀린 단어가 없습니다! 아주 훌륭해요 💯</td></tr>`;
                switchView('study-view');
                return;
            }

            words.forEach(w => {
                const visualMeaning = w.meaning ? w.meaning.replace(/\n/g, '<br>') : '';
                tbody.innerHTML += `<tr>
                    <td class="fw-bold text-danger fs-5">${w.word}</td>
                    <td class="text-start ps-4">${visualMeaning}</td>
                    <td><span class="badge bg-danger fs-6">${w.wrongCount}회 틀림</span></td>
                </tr>`;
            });

            switchView('study-view');
        })
        .catch(err => console.error("오답 노트 로드 실패:", err));
}

// 홈 화면 전환 및 테마 초기화
function showHome() {
    loadDates();

    const cardHeader = document.getElementById('study-card-header');
    if (cardHeader) {
        cardHeader.className = 'card-header bg-success text-white d-flex justify-content-between align-items-center';
    }
    const examBtn = document.getElementById('btn-date-exam');
    if (examBtn) {
        examBtn.classList.remove('d-none');
    }

    switchView('home-view');
}

// 화면 토글 유틸
function switchView(viewId) {
    document.getElementById('home-view').classList.add('hidden');
    document.getElementById('study-view').classList.add('hidden');
    document.getElementById('exam-view').classList.add('hidden');
    document.getElementById(viewId).classList.remove('hidden');
}