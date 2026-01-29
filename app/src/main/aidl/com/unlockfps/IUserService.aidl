package com.unlockfps;

interface IUserService {
    void destroy() = 1;
    String saveFile(String path, in byte[] content) = 2;
    String findLatestVersion(String basePath) = 3;
}