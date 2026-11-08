let controller = (() => {
    // ALL DATA
    let DOMData = {
        dials: '.dial', dot: '.dot', cutLine: '.cut_line', clock: '',
        minuteLine: '.minute_line', hourLine: '.hour_line', clockLength: 360,
        triangleManager: '.triangle_manager', triangle: '.triangle', shadow: '.shadow',
        container: '.container', roundImage: '.round_img',
    };

    let clock, clockLength, second, minute, modMinute, hour, modHour;
    clockLength = DOMData.clockLength;

    let writeHtml = () => {
        clock = DOMData.clock;

        // REPEAT CLOCK DIAL 360 TIMES
        for (let i = 0; i < clockLength; i++) {
            clock +=
            `
                <span class="dial">
                    <div class="cut_line"></div>
                </span>
            `;
        }

        // ADD HTML AFTER LOOP FINISH
        clock +=
        `
            <div class="dot"></div>
            <div class="minute_line"></div>
            <div class="hour_line"></div>
            <div class="triangle_manager">
                <div class="triangle"></div>
                <div class="shadow"></div>
            </div>
        `;

        // INSERT ABOVE HTML INTO CONTAINER CLASS
        document.querySelector(DOMData.container).innerHTML = clock;
    };

    // MAKE DIALS IN UI
    let makeDials = () => {
        for (let i = 1; i <= clockLength; i++) {
            document.querySelector(`${DOMData.dials}:nth-child(${i})`).style.transform = `rotate(${(i - 1)}deg)`;
        }
    };

    let calcClock = () => {
        second = new Date().getSeconds() * 6;

        let calcHourAndMin = () => {

            // CALCULATE MINUTES
            let calcMinutes = () => {
                minute = new Date().getMinutes();
                modMinute = minute * 6;
                document.querySelector(DOMData.minuteLine).style.transform = `rotate(${180 + modMinute}deg)`;
            };

            // CALCULATE HOURS
            let calcHours = () => {
                hour = new Date().getHours();
                minute = new Date().getMinutes();
                modHour = hour % 12 / 12 * 360 + ((minute * 6) / 12);
                document.querySelector(DOMData.hourLine).style.transform = `rotate(${180 + modHour}deg)`;
            };

            // RETURN OF calcClock
            return {
                calcMinutes: () => calcMinutes(),
                calcHours: () => calcHours()
            }
        };

        // CALCULATE SECONDS
        let calcSecond = () => {
            second++;
            document.querySelector(DOMData.triangleManager).style.transform = `rotate(${180 + second}deg)`;
            document.querySelector(`${DOMData.container} span:nth-of-type(${((second - 4) % 360 + 1)}) ${DOMData.cutLine}`).classList.add('one');
            document.querySelector(`${DOMData.container} span:nth-of-type(${((second - 30 + 360) % 360 + 1)}) ${DOMData.cutLine}`).classList.remove('one');
        };

        return {
            getCalcSecond: () => calcSecond(),
            getCalcMinutes: () => calcHourAndMin().calcMinutes(),
            getCalcHours: () => calcHourAndMin().calcHours()
        }
    };

    // FINAL FUNCTION FOR CLOCK
    let clockReady = () => {
        writeHtml();
        makeDials();
        setInterval(calcClock().getCalcMinutes, 1000);
        setInterval(calcClock().getCalcHours, 1000);
        setInterval(calcClock().getCalcSecond, (60 / 360) * 1000);
    };

    // RETURN OF controller
    return {
        init: () => clockReady()
    }
})();

controller.init();
