using System;
using System.Threading;
using System.Threading.Tasks;

namespace Registration.Web.Utils
{
    public static class TimerTaskFactory
    {
        private static readonly TimeSpan DoNotRepeat = TimeSpan.FromMilliseconds(-1);

        /// <summary>
        /// Starts a new task that will poll for a result using the specified function, and will be completed when it satisfied the specified condition.
        /// </summary>
        /// <typeparam name="T">The type of value that will be returned when the task completes.</typeparam>
        /// <param name="getResult">Function that will be used for polling.</param>
        /// <param name="isResultValid">Predicate that determines if the result is valid, or if it should continue polling</param>
        /// <param name="pollInterval">Polling interval.</param>
        /// <param name="timeout">The timeout interval.</param>
        /// <returns>The result returned by the specified function, or <see langword="null"/> if the result is not valid and the task times out.</returns>
        public static Task<T> StartNew<T>(Func<T> getResult, Func<T, bool> isResultValid, TimeSpan pollInterval, TimeSpan timeout)
        {
            Timer timer = null;
            TaskCompletionSource<T> taskCompletionSource = null;
            DateTime expirationTime = DateTime.UtcNow.Add(timeout);

            timer =
                new Timer(_ =>
                {
                    try
                    {
                        if (DateTime.UtcNow > expirationTime)
                        {
                            timer.Dispose();
                            taskCompletionSource.SetResult(default(T));
                            return;
                        }

                        var result = getResult();

                        if (isResultValid(result))
                        {
                            timer.Dispose();
                            taskCompletionSource.SetResult(result);
                        }
                        else
                        {
                            // try again
                            timer.Change(pollInterval, DoNotRepeat);
                        }
                    }
                    catch (Exception e)
                    {
                        timer.Dispose();
                        taskCompletionSource.SetException(e);
                    }
                });

            taskCompletionSource = new TaskCompletionSource<T>(timer);

            timer.Change(pollInterval, DoNotRepeat);

            return taskCompletionSource.Task;
        }
    }
}
