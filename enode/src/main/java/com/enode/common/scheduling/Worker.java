package com.enode.common.scheduling;

import com.enode.common.function.Action;
import com.enode.common.logging.ENodeLogger;
import org.slf4j.Logger;

public class Worker {
    private static final Logger _logger = ENodeLogger.getLog();

    private Object _lockObject = new Object();
    private String _actionName;
    private Action _action;
    private Status _status;
    private Thread thread;

    public String actionName() {
        return _actionName;
    }

    public Worker(String actionName, Action action) {
        _actionName = actionName;
        _action = action;
        _status = Status.Initial;
    }

    public Worker start() {
        synchronized (_lockObject) {
            if (_status.equals(Status.Running)) {
                return this;
            }

            _status = Status.Running;

            thread = new Thread(this::loop, String.format("%s.Worker", _actionName));

            thread.setDaemon(true);

            thread.start();

            return this;
        }
    }

    public Worker stop() {
        synchronized (_lockObject) {
            if (_status.equals(Status.StopRequested)) {
                return this;
            }

            _status = Status.StopRequested;

            thread.interrupt();

            _logger.info("Worker thread shutdown,thread id:{}", thread.getName());

            return this;
        }
    }

    private void loop() {
        while (this._status == Status.Running) {
            try {
                _action.apply();
            } catch (InterruptedException e) {
                if (_status != Status.StopRequested) {
                    _logger.info("Worker thread caught ThreadAbortException, try to resetting, actionName:{}", _actionName);
                    _logger.info("Worker thread ThreadAbortException resetted, actionName:{}", _actionName);
                }
            } catch (Exception ex) {
                _logger.error(String.format("Worker thread has exception, actionName:%s", _actionName), ex);
            }
        }
    }

    enum Status {
        Initial,
        Running,
        StopRequested
    }
}
