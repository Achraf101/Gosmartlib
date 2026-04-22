export class DelayedLoader {
  private timer: number | null = null;
  public isLoading = false;
  public visible = false;

  start(delay = 1000): void {
    this.clearTimer();
    this.isLoading = true;
    this.visible = false;

    this.timer = window.setTimeout(() => {
      this.timer = null;
      if (this.isLoading) {
        this.visible = true;
      }
    }, delay);
  }

  stop(): void {
    this.isLoading = false;
    this.visible = false;
    this.clearTimer();
  }

  private clearTimer(): void {
    if (this.timer !== null) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
  }
}
