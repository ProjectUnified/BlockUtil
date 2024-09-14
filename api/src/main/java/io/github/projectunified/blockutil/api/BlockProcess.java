package io.github.projectunified.blockutil.api;

public interface BlockProcess {
    BlockProcess COMPLETED = new BlockProcess() {
        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public void cancel() {
            // Do nothing
        }
    };

    boolean isDone();

    void cancel();
}
