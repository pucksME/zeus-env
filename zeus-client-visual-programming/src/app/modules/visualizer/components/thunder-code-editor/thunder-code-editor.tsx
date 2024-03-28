import React, { useEffect, useRef, useState } from 'react';

import './thunder-code-editor.module.scss';
import { useStore } from '../../../../store';
import Window from '../../../../components/window/window';
import spacing from '../../../../../assets/styling/spacing.json';
import colors from '../../../../../assets/styling/colors.json';
import zIndices from '../../../../../assets/styling/z-indices.json';
import { ThunderCodeEditorUtils } from '../../thunder-code-editor.utils';
import { Box, IconButton, Typography } from '@material-ui/core';
import { CodeModuleService } from '../../services/code-module.service';
import { AppUtils } from '../../../../app.utils';
import { ErrorDto } from '../../../../../gen/api-client';
import { useQueryClient } from 'react-query';
import { QueryKeys } from '../../../../enums/query-keys.enum';
import DeleteIcon from '@material-ui/icons/Delete';
import { useDeleteCodeModule } from '../../data/code-module-data.hooks';
import { ThunderCodeEditorProperties } from '../../../../interfaces/thunder-code-editor-properties.interface';

export interface ThunderCodeEditorProps {
  projectUuid: string;
  componentUuid: string;
  thunderCodeEditorProperties: ThunderCodeEditorProperties;
}

const update = AppUtils.debounce<Promise<ErrorDto[]>>(CodeModuleService.update, 500);

export function ThunderCodeEditor(props: ThunderCodeEditorProps) {
  const setThunderCodeEditorProperties = useStore(state => state.setThunderCodeEditorProperties);

  const thunderCodeEditorMargin = 25;
  const [position, setPosition] = useState<{ x: number, y: number }>({
    x: spacing.toolbar.width + spacing.toolbox.width + thunderCodeEditorMargin,
    y: thunderCodeEditorMargin
  });
  const initialContentHeight = 500;
  const [height, setHeight] = useState<number>(initialContentHeight);
  const [linesOfCode, setLinesOfCode] = useState<number>(0);
  const [code, setCode] = useState<string>(props.thunderCodeEditorProperties.initialCode);
  const [errors, setErrors] = useState<ErrorDto[]>([]);
  const [currentLine, setCurrentLine] = useState<number>(0);

  // https://stackoverflow.com/a/38386230 [accessed 1/6/2023, 17:42]
  // https://stackoverflow.com/a/62250053 [accessed 1/6/2023, 17:42]
  // https://stackoverflow.com/a/56173415 [accessed 1/6/2023, 17:42]
  const textAreaRef = useRef<HTMLTextAreaElement | null>(null);
  // https://mattclaffey.medium.com/adding-react-refs-to-an-array-of-items-96e9a12ab40c [accessed 7/6/2023, 15:22]
  const lineNumberRefs = useRef<HTMLDivElement[]>([]);
  const contentRef = useRef<HTMLDivElement | null>(null);

  const queryClient = useQueryClient();
  const deleteCodeModule = useDeleteCodeModule(props.projectUuid, props.componentUuid);

  const thunderCodeEditorPadding = 10;
  const lineNumbersWidth = 50;
  const lineNumbersMargin = 10;
  const lineHeight = 18;

  const updateErrors = async (code: string) =>
    setErrors(await CodeModuleService.checkCode({ code }));

  useEffect(() => {
    if (textAreaRef.current === null) {
      return;
    }

    setLinesOfCode(ThunderCodeEditorUtils.getLinesOfCode(textAreaRef.current.value));
  }, []);

  useEffect(() => setLinesOfCode(ThunderCodeEditorUtils.getLinesOfCode(code)), [code]);

  useEffect(() => {
    if (!props.thunderCodeEditorProperties.active || code === '') {
      return;
    }

    updateErrors(code);
  }, [props.thunderCodeEditorProperties.active]);

  if (!props.thunderCodeEditorProperties.active) {
    return null;
  }

  const buildLineNumbers = () => {
    const lineNumbers = [];

    for (let i = 1; i <= linesOfCode; i++) {
      lineNumbers.push(<div
        key={i}
        ref={(divElement) => lineNumberRefs.current[i] = divElement}
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          height: lineHeight
      }}>
        <Typography
          variant={'overline'}
          color={'textSecondary'}
          style={{fontSize: '0.65rem'}}
        >{i}</Typography>
      </div>);
    }

    return <div style={{
      borderRight: '1px solid ' + colors.border_light,
      boxSizing: 'border-box',
      marginRight: lineNumbersMargin,
      paddingBottom: thunderCodeEditorPadding,
      paddingTop: thunderCodeEditorPadding,
      width: lineNumbersWidth
    }}>{lineNumbers}</div>;
  };

  const buildCurrentLineIndication = () => (currentLine === 0)
    ? null
    : <div style={{
      backgroundColor: colors.secondary.main,
      height: lineHeight,
      width: '100%',
      opacity: 0.1,
      // https://stackoverflow.com/a/4839672 [accessed 2/6/2023, 10:21]
      pointerEvents: 'none',
      position: 'absolute',
      left: 0,
      top: ((currentLine - 1) * lineHeight) + thunderCodeEditorPadding,
      zIndex: zIndices.thunderCodeEditor.currentLineIndication
    }}></div>;

  const buildErrors = () => {
    if (contentRef.current === null || height === 0) {
      return null;
    }

    const hiddenLinesTopCount = contentRef.current.scrollTop / lineHeight;
    const visibleLinesCount = height / lineHeight;

    return errors.map((error, index) => {
      if (error.line <= hiddenLinesTopCount || error.line > hiddenLinesTopCount + visibleLinesCount) {
        return null;
      }

      const paddingVertical = 4;
      const lineNumberRef = lineNumberRefs.current[error.line];

      if (lineNumberRef === undefined) {
        return null;
      }

      return <Box
        key={index}
        style={{
          backgroundColor: colors.primary.main,
          borderRadius: 5,
          height: lineHeight + paddingVertical,
          position: 'absolute',
          left: (
            ThunderCodeEditorUtils.getNumberOfCharactersInLine(code, error.line) * 8
          ) + lineNumbersWidth + lineNumbersMargin,
          // https://stackoverflow.com/a/11396681 [accessed 7/6/2023, 17:46]
          top: lineNumberRef.getBoundingClientRect().y - position.y - spacing.navigation.height - (paddingVertical / 2),
          paddingLeft: 10,
          paddingRight: 10,
          whiteSpace: 'nowrap'
        }}
        boxShadow={1}
      >
        <Typography
          variant={'caption'}
          style={{ color: colors.primary.contrastText }}
        >
          {error.message}
        </Typography>
      </Box>;
    });
  };

  const handleChangePosition = (position: { x: number, y: number }) => setPosition(position);

  const handleMouseDown = (event: React.MouseEvent<HTMLDivElement>) => event.stopPropagation();

  const handleChange = async (event: React.ChangeEvent<HTMLTextAreaElement>) => {
    const code = event.currentTarget.value;
    setErrors([]);
    setCode(code);
    const errors = await update(props.thunderCodeEditorProperties.codeModuleUuid, { code });

    if (code !== '') {
      setErrors(errors);
    }

    await queryClient.invalidateQueries(QueryKeys.PROJECT_CODE_MODULES + props.projectUuid);
    await queryClient.invalidateQueries(QueryKeys.VISUALIZER_WORKSPACE + props.componentUuid);
  };

  const handleSelect = (event: React.SyntheticEvent<HTMLTextAreaElement>) => setCurrentLine(
    ThunderCodeEditorUtils.getLineNumber(code, event.currentTarget.selectionStart)
  );

  const handleResize = (height: number, width: number) => setHeight(height);

  const handleScroll = (event: React.UIEvent<HTMLDivElement>) => {
    setErrors([...errors]);
  };

  const handleClose = () => setThunderCodeEditorProperties({
    ...props.thunderCodeEditorProperties,
    active: false
  });

  const handleDelete = () => {
    setThunderCodeEditorProperties({...props.thunderCodeEditorProperties, active: false});
    deleteCodeModule.mutate(props.thunderCodeEditorProperties.codeModuleUuid);
  }

  const buildActions = () => (
    <div>
      <IconButton size={'small'} onClick={handleDelete}>
        <DeleteIcon fontSize={'small'}/>
      </IconButton>
    </div>
  )

  return (
    <Window
      id={'thunder-code-editor'}
      title={'Thunder Code Editor'}
      visible={true}
      position={position}
      onChangePosition={handleChangePosition}
      onClose={handleClose}
      contentRef={contentRef}
      initialContentHeight={initialContentHeight}
      initialContentWidth={500}
      minContentHeight={250}
      minContentWidth={250}
      contentStyle={{overflowY: 'auto', position: 'relative'}}
      onResize={handleResize}
      onContentScroll={handleScroll}
      overlay={buildErrors()}
      actions={buildActions()}
    >
      <div
        style={{display: 'flex', minHeight: '100%'}}
        onMouseDown={handleMouseDown}
      >
        {buildLineNumbers()}
        <textarea
          ref={textAreaRef}
          spellCheck={false}
          style={{
            border: 'none',
            boxSizing: 'border-box',
            minHeight: '100%',
            lineHeight: lineHeight + 'px',
            outline: 'none',
            overflowX: 'hidden',
            width: '100%',
            paddingBottom: thunderCodeEditorPadding,
            paddingTop: thunderCodeEditorPadding,
            resize: 'none',
            //whiteSpace: 'nowrap'
        }}
          onChange={handleChange}
          onSelect={handleSelect}
          value={code}
        ></textarea>
      </div>
      {buildCurrentLineIndication()}
    </Window>
  );
}

export default ThunderCodeEditor;
