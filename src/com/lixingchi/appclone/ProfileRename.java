package com.lixingchi.appclone;

/** Short-lived root rename; profile identity, data and running state are unchanged. */
public final class ProfileRename {
    interface Device {
        long verify(int user) throws Exception;
        String name(int user) throws Exception;
        void name(int user, String name) throws Exception;
    }

    static void validate(String name) {
        if (name == null || name.isEmpty() || name.indexOf('\0') >= 0
                || name.indexOf('\n') >= 0 || name.indexOf('\r') >= 0)
            throw new IllegalArgumentException("请输入非空的单行名称，不能包含 NUL 字符。");
        // Android truncates persisted user names to 100 UTF-16 code units.
        if (name.length() > 100) throw new IllegalArgumentException("最多 100 个字符（表情可能按两个计算）。");
    }

    static String command(int user, long serial, String name) {
        validate(name);
        if (user <= 0 || serial < 0) throw new IllegalArgumentException("无效的分身标识。");
        return ProfileStart.entry(ProfileRename.class) + " " + user + " " + serial + " " + Clones.quote(name);
    }

    static void rename(int user, long serial, String name, Device device) throws Exception {
        validate(name);
        if (user <= 0 || serial < 0) throw new IllegalArgumentException("无效的分身标识。");
        verify(user, serial, device);
        if (name.equals(device.name(user))) return;
        verify(user, serial, device);
        device.name(user, name);
        verify(user, serial, device);
        if (!name.equals(device.name(user)))
            throw new IllegalStateException("无法确认新名称，请刷新后重试。");
    }

    private static void verify(int user, long serial, Device device) throws Exception {
        if (device.verify(user) != serial)
            throw new IllegalStateException("分身身份已变化，请刷新后重试。");
    }

    public static void main(String[] args) {
        try {
            if (android.os.Process.myUid() != 0) throw new IllegalStateException("需要 Root 权限。");
            if (args.length != 3) throw new IllegalArgumentException("需要用户、序号和名称。");
            rename(Integer.parseInt(args[0]), Long.parseLong(args[1]), args[2], new AndroidDevice());
            System.out.println("分身已重命名。");
        } catch (Exception error) {
            Throwable cause = error;
            while (cause.getCause() != null) cause = cause.getCause();
            System.err.println("无法重命名分身：" + cause);
            System.exit(1);
        }
    }

    private static final class AndroidDevice extends ProfileStart.AndroidDevice implements Device {
        AndroidDevice() throws Exception { }
        @Override public String name(int user) throws Exception {
            Object info = users.getMethod("getUserInfo", int.class).invoke(manager, user);
            if (info == null) throw new IllegalStateException("分身已不存在。");
            return (String) info.getClass().getField("name").get(info);
        }
        @Override public void name(int user, String name) throws Exception {
            users.getMethod("setUserName", int.class, String.class).invoke(manager, user, name);
        }
    }
}
