// 파일크기포맷, 허용상수, 파일 유효성 검사 및 추가 로직

// 파일 유효성 검사 상수
export const allowedExtensions = ['xlsx', 'pptx', 'txt', 'pdf', 'jpg', 'jpeg', 'png', 'hwp'];
export const maxCount = 5;
export const maxSize = 20 * 1024 * 1024; // 20MB (기존 로직 기준)

// 파일 크기 포맷 함수
export const formatBytes = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

/**
 * 파일 유효성을 검사하고 유효한 파일 목록을 반환합니다.
 * @param {FileList} files - 새로 선택된 파일 목록
 * @param {Array} uploadedFiles - 현재 새로 업로드 대기 중인 파일 (File 객체)
 * @param {Array} existingFiles - 기존에 서버에 저장된 파일 (DTO 객체)
 * @param {Array} filesToDeleteIds - 삭제 예정인 기존 파일 ID 목록
 * @returns {File[]} 유효한 File 객체 배열
 */
export const validateAndGetFiles = (files, uploadedFiles, existingFiles, filesToDeleteIds) => {
    const newFiles = Array.from(files);
    let validFiles = [];
    
    // 현재 첨부 가능한 파일의 총 개수 (기존 파일 중 삭제 예정이 아닌 파일 + 새로 업로드된 파일)
    const currentFileCount = existingFiles.filter(f => !filesToDeleteIds.includes(f.boardFileId)).length + uploadedFiles.length;

    for (const file of newFiles) {
        const ext = file.name.split('.').pop().toLowerCase();

        // 1. 확장자 검사
        if (!allowedExtensions.includes(ext)) {
            alert(`${file.name}: 지원하지 않는 형식입니다.`);
            continue;
        }

        // 2. 파일 개수 제한 검사
        if (currentFileCount + validFiles.length >= maxCount) {
            alert(`최대 ${maxCount}개까지 업로드 할 수 있습니다`);
            break;
        }

        // 3. 파일 크기 검사
        if (file.size > maxSize) {
            alert(`${file.name} : ${formatBytes(maxSize)} 초과`);
            continue;
        }

        // 4. 중복 파일명 검사 (기존 파일명 및 새로 업로드될 파일명 모두 체크)
        const isDuplicate = existingFiles.some(f => f.boardFileName === file.name && !filesToDeleteIds.includes(f.boardFileId)) ||
                            uploadedFiles.some(f => f.name === file.name) ||
                            validFiles.some(f => f.name === file.name);

        if (isDuplicate) {
            alert(`${file.name}은 이미 업로드 되어 있습니다.`);
            continue;
        }
        validFiles.push(file);
    }
    return validFiles;
};